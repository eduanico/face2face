package com.eclipsoft.face2face.web.rest;

import com.eclipsoft.face2face.Integration.CheckIdClient;
import com.eclipsoft.face2face.domain.enumeration.EventType;
import com.eclipsoft.face2face.service.AgentService;
import com.eclipsoft.face2face.service.BlackListService;
import com.eclipsoft.face2face.service.EventService;
import com.eclipsoft.face2face.service.ImageService;
import com.eclipsoft.face2face.service.dto.EventDTO;
import com.eclipsoft.face2face.web.rest.vm.RequestVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


@RestController
@RequestMapping("/api")
public class ImageResource {

    private final Logger log = LoggerFactory.getLogger(ImageResource.class);

    private final ImageService imageService;

    private final AgentService agentService;

    private final EventService eventService;

    private final CheckIdClient checkIdClient;

    private final BlackListService blackListService;

    private static final String IS_SUCCESSFUL = "isSuccessful";
    private static final String DETAIL = "detail";


    public ImageResource(ImageService imageService, AgentService agentService,
                         EventService eventService, CheckIdClient checkIdClient, BlackListService blackListService) {
        this.imageService = imageService;
        this.agentService = agentService;
        this.eventService = eventService;
        this.checkIdClient = checkIdClient;
        this.blackListService = blackListService;
    }


    /**
     * Validates an array of images with an id for reference
     */
    @PostMapping(value = "/validate-face")
    public Mono<ResponseEntity<Map<String, Object>>> validateEvidence(@RequestPart("images") Flux<FilePart> images
            , @RequestPart("id") String id, Authentication authentication) {
        return blackListService.existInBlackList(id)
                .flatMap(exists -> {
                    Map<String, Object> response = new HashMap<>();
                    if (Boolean.TRUE.equals(exists)) {
                        response.put(IS_SUCCESSFUL, false);
                        response.put(DETAIL, "Número máximo de intentos excedidos por hoy");
                        return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
                    }

                    EventDTO eventDTO = new EventDTO();
                    AtomicInteger count = new AtomicInteger(1);
                    AtomicBoolean flag = new AtomicBoolean();

                    agentService.findByName(authentication.getName()).subscribe(
                            agentDTO -> {
                                eventDTO.setAgent(agentDTO);
                                eventDTO.setIdentification(id);
                                eventDTO.setValidationDate(Instant.now());
                            });

                    List<FilePart> list;
                    try {
                        list = images.collectList().toFuture().get();

                        for (FilePart filePart : list) {
                            List<DataBuffer> dblist;
                            dblist = filePart.content().collectList().toFuture().get();

                            for (DataBuffer d : dblist) {
                                flag.set(imageService.uploadAndValidateImages(id, d.asByteBuffer(), count.get(), list.size(), eventDTO));
                                count.getAndIncrement();
                                if (!flag.get()) {
                                    eventDTO.setEventType(EventType.VALIDATION_FAILED);
                                    eventDTO.setSuccessful(false);
                                    eventService.save(eventDTO).subscribe();
                                    response.put(IS_SUCCESSFUL, false);
                                    response.put(DETAIL, eventDTO.getDetail());
                                    log.info("VALIDATION FAILED FOR ID: {}", id);
                                    return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
                                }
                            }
                        }
                        response.put(IS_SUCCESSFUL, true);
                        response.put(DETAIL, eventDTO.getDetail());
                        eventDTO.setEventType(EventType.VALIDATION_SUCCESS);
                        eventDTO.setSuccessful(true);
                        eventService.save(eventDTO).subscribe();
                        log.info("VALIDATION SUCCESS FOR ID: {}", id);
                        return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Validates photos with another reactive approach - on test
     */
    @PostMapping(value = "/validate-photos")
    public Mono<ResponseEntity<Boolean>> validateEvidences2(@RequestPart("images") Flux<FilePart> images
            , @RequestPart("id") String id, Authentication authentication) {
        EventDTO eventDTO = new EventDTO();

        agentService.findByName(authentication.getName()).subscribe(
                agentDTO -> {
                    eventDTO.setAgent(agentDTO);
                    eventDTO.setIdentification(id);
                    eventDTO.setValidationDate(Instant.now());
                });
        AtomicInteger count = new AtomicInteger(1);
        AtomicBoolean flag = new AtomicBoolean();
        return images.collectList().flatMap(list -> {
            for (FilePart f : list) {
                List<DataBuffer> dblist;
                try {
                    dblist = f.content().collectList().toFuture().get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                for (DataBuffer d : dblist) {
                    flag.set(imageService.uploadAndValidateImages(id, d.asByteBuffer(),
                            count.get(), list.size(), eventDTO));
                    count.getAndIncrement();
                    if (!flag.get()) {
                        eventDTO.setEventType(EventType.VALIDATION_FAILED);
                        eventDTO.setSuccessful(false);
                        log.info("VALIDATION FAILED");
                        eventService.save(eventDTO).subscribe();
                        return Mono.just(new ResponseEntity<>(false, HttpStatus.OK));
                    }
                }
            }
            eventDTO.setEventType(EventType.VALIDATION_SUCCESS);
            eventDTO.setSuccessful(true);
            log.info("VALIDATION SUCCESS");
            eventService.save(eventDTO).subscribe();
            return Mono.just(new ResponseEntity<>(true, HttpStatus.OK));
        });
    }

    /**
     * Fetch the image in base64 from RCE
     */
    @PostMapping(value = "/check-id", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> uploadImage(@Valid @RequestBody RequestVM referenceModel) {
        String id = referenceModel.getId();
        return blackListService.existInBlackList(id).flatMap(flag -> {
            Map<String, Object> response = new HashMap<>();
            if (Boolean.TRUE.equals(flag)) {
                response.put(IS_SUCCESSFUL, false);
                response.put(DETAIL, "Número máximo de intentos excedidos por hoy");
                return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));

            } else if (imageService.referenceExistsInS3(id)) {
                response.put(IS_SUCCESSFUL, true);
                response.put(DETAIL, "Ok");
                return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
            }

            return checkIdClient.findPerson(referenceModel.getId(), referenceModel.getDactilar())
                    .flatMap(person -> {
                        imageService.uploadBase64ToS3(referenceModel.getId(),
                                (String) person.get("fotografia"), "pruebas-id4face");
                        response.put(IS_SUCCESSFUL, true);
                        response.put(DETAIL, "Ok");
                        return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
                    })
                    .doOnError(throwable -> Mono.just(ResponseEntity.badRequest().body(throwable.getMessage())));
        });
    }

    /**
     * Receives the image in base64 from client
     */
    @PostMapping(value = "/upload-base64", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadReferenceImage(@RequestBody RequestVM referenceModel) {
        imageService.uploadBase64ToS3(referenceModel.getId(), referenceModel.getImage(), "pruebas-id4face");
        return ResponseEntity.ok().build();
    }

    /**
     * Validates source and target photo without checkid and id
     */
    @PostMapping(value = "/validate-photo")
    public Mono<ResponseEntity<Map<String, Object>>> validateImage(@RequestPart("source") Mono<FilePart> source
            , @RequestPart("target") Mono<FilePart> target, Authentication authentication) throws ExecutionException, InterruptedException {
        EventDTO eventDTO = new EventDTO();
        AtomicInteger count = new AtomicInteger(1);
        AtomicBoolean flag = new AtomicBoolean();
        Map<String, Object> response = new HashMap<>();
        String id = UUID.randomUUID().toString().substring(0, 10);
        agentService.findByName(authentication.getName()).subscribe(
                agentDTO -> {
                    eventDTO.setAgent(agentDTO);
                    eventDTO.setIdentification(id);
                    eventDTO.setValidationDate(Instant.now());
                });

        FilePart sourceImage = source.toFuture().get();
        FilePart targetImage = target.toFuture().get();
        List<DataBuffer> sourceDb = sourceImage.content().collectList().toFuture().get();
        List<DataBuffer> targetDb = targetImage.content().collectList().toFuture().get();


        flag.set(imageService.uploadAndValidateSourceAndTarget(id, sourceDb.get(0).asByteBuffer(), targetDb.get(0).asByteBuffer(), eventDTO));
        count.getAndIncrement();
        if (!flag.get()) {
            eventDTO.setEventType(EventType.VALIDATION_FAILED);
            eventDTO.setSuccessful(false);
            eventService.save(eventDTO).subscribe();
            response.put(IS_SUCCESSFUL, false);
            response.put(DETAIL, eventDTO.getDetail());
            log.info("VALIDATION FAILED FOR ID: {}", id);
            return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
        }

        response.put(IS_SUCCESSFUL, true);
        response.put(DETAIL, eventDTO.getDetail());
        eventDTO.setEventType(EventType.VALIDATION_SUCCESS);
        eventDTO.setSuccessful(true);
        eventService.save(eventDTO).

                subscribe();
        log.info("VALIDATION SUCCESS FOR ID: {}", id);
        return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
    }

}
