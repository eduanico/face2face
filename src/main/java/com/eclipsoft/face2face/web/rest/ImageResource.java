package com.eclipsoft.face2face.web.rest;

import com.eclipsoft.face2face.Integration.CheckIdClient;
import com.eclipsoft.face2face.domain.enumeration.EventType;
import com.eclipsoft.face2face.service.AgentService;
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

    public ImageResource(ImageService imageService, AgentService agentService,
                         EventService eventService, CheckIdClient checkIdClient) {
        this.imageService = imageService;
        this.agentService = agentService;
        this.eventService = eventService;
        this.checkIdClient = checkIdClient;
    }


    /**
     * Validates an array of images with an id for reference
     */
    @PostMapping(value = "/validate-face")
    public Mono<ResponseEntity<Map<String,Object>>> validateEvidences(@RequestPart("images") Flux<FilePart> images
        , @RequestPart("id") String id, Authentication authentication) throws ExecutionException, InterruptedException
    {
        EventDTO eventDTO = new EventDTO();
        AtomicInteger count = new AtomicInteger(1);
        AtomicBoolean flag = new AtomicBoolean();
        Map<String,Object> response = new HashMap<>();

        agentService.findByName(authentication.getName()).subscribe(
            agentDTO -> {
                eventDTO.setAgent(agentDTO);
                eventDTO.setIdentification(id);
                eventDTO.setValidationDate(Instant.now());
            });

        List<FilePart> list = images.collectList().toFuture().get();

        for (FilePart filePart: list) {
            List<DataBuffer> dblist = filePart.content().collectList().toFuture().get();

            for (DataBuffer d: dblist) {
                flag.set(imageService.uploadAndValidateImages(id, d.asByteBuffer(), count.get(), list.size(), eventDTO));
                count.getAndIncrement();
                if (!flag.get()) {
                    eventDTO.setEventType(EventType.VALIDATION_FAILED);
                    eventDTO.setSuccessful(false);
                    eventService.save(eventDTO).subscribe();
                    response.put("isSuccessful", false);
                    response.put("detail", eventDTO.getDetail());
                    log.info( "VALIDATION FAILED FOR ID: {}", id);
                    return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
                }
            }
        }
        response.put("isSuccessful", true);
        response.put("detail", eventDTO.getDetail());
        eventDTO.setEventType(EventType.VALIDATION_SUCCESS);
        eventDTO.setSuccessful(true);
        eventService.save(eventDTO).subscribe();
        log.info("VALIDATION SUCCESS FOR ID: {}", id);
        return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
    }

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
            for (FilePart f: list) {
                List<DataBuffer> dblist;
                try {
                    dblist = f.content().collectList().toFuture().get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                for (DataBuffer d: dblist) {
                    flag.set(imageService.uploadAndValidateImages(id, d.asByteBuffer(),
                        count.get(), list.size(), eventDTO));
                    count.getAndIncrement();
                    if (!flag.get()) {
                        eventDTO.setEventType(EventType.VALIDATION_FAILED);
                        eventDTO.setSuccessful(false);
                        log.info("VALIDATION FAILED");
                        eventService.save(eventDTO).subscribe();
                        return Mono.just(new ResponseEntity<>(false,HttpStatus.OK));
                    }
                }
            }
            eventDTO.setEventType(EventType.VALIDATION_SUCCESS);
            eventDTO.setSuccessful(true);
            log.info("VALIDATION SUCCESS");
            eventService.save(eventDTO).subscribe();
            return Mono.just(new ResponseEntity<>(true,HttpStatus.OK));
        });
    }

    /**
     * Fetch the image in base64 from RCE
     */
    @PostMapping(value = "/upload-image", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> uploadImage(@Valid @RequestBody RequestVM referenceModel){

        return checkIdClient.findPerson(referenceModel.getId(), referenceModel.getDactilar())
                .flatMap(person-> {
                    imageService.uploadBase64ToS3(referenceModel.getId(),
                        (String) person.get("fotografia"), "pruebas-id4face");
                    return Mono.just(ResponseEntity.ok().build());
                })
            .doOnError(throwable -> Mono.just(ResponseEntity.badRequest().body(throwable.getMessage())));
    }

    /**
     * Receives the image in base64 from client
     */
    @PostMapping(value = "/upload-base64", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadReferenceImage(@RequestBody RequestVM referenceModel){
        imageService.uploadBase64ToS3(referenceModel.getId(), referenceModel.getImage(), "pruebas-id4face");
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/principal")
    public ResponseEntity<String> getPrincipalName(Authentication authentication) {
        return ResponseEntity.ok(authentication.getName());
    }
}
