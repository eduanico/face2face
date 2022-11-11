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
import java.io.*;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api")
public class ImageResource {

    private final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final ImageService imageService;

    private final AgentService agentService;

    private final EventService eventService;

    private final CheckIdClient checkIdClient;

    public ImageResource(ImageService imageService, AgentService agentService, EventService eventService, CheckIdClient checkIdClient) {
        this.imageService = imageService;
        this.agentService = agentService;
        this.eventService = eventService;
        this.checkIdClient = checkIdClient;
    }

    /**
     * Validates an array of images with an id for reference
     *
     * @param images
     * @param id
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/validate")
    public Mono<ResponseEntity> validateEvidences(@RequestPart("images") Flux<FilePart> images
        , @RequestPart("id") String id, Authentication authentication) throws ExecutionException, InterruptedException {
        EventDTO eventDTO = new EventDTO();
        AtomicInteger count = new AtomicInteger(1);
        AtomicBoolean flag = new AtomicBoolean();

        agentService.findByName(authentication.getName()).subscribe(
            agentDTO -> {
                eventDTO.setAgent(agentDTO);
                eventDTO.setIdentification(id);
                eventDTO.setValidationDate(Instant.now());
            });

        List<FilePart> list = images.collectList().toFuture().get();
        for(FilePart filePart: list){
            List<DataBuffer> dblist = filePart.content().collectList().toFuture().get();

            for(DataBuffer d: dblist) {
                flag.set(imageService.uploadAndValidateImages(id, d.asByteBuffer(), count.get(), list.size()));
                count.getAndIncrement();
                if (!flag.get()) {
                    eventDTO.setEventType(EventType.VALIDATION_FAILED);
                    eventDTO.setSuccessful(false);
                    eventService.save(eventDTO).subscribe();
                    log.info("VALIDATION FAILED");
                    return Mono.just(new ResponseEntity(false, HttpStatus.OK));
                }
            }
        }
        eventDTO.setEventType(EventType.VALIDATION_SUCCESS);
        eventDTO.setSuccessful(true);
        eventService.save(eventDTO).subscribe();
        log.info("VALIDATION SUCCESS");
        return Mono.just(new ResponseEntity(true, HttpStatus.OK));
    }

    @PostMapping(value = "/validate-photos")
    public Mono<ResponseEntity> validateEvidences2(@RequestPart("images") Flux<FilePart> images
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
            for(FilePart f: list){
                List<DataBuffer> dblist = null;
                try {
                    dblist = f.content().collectList().toFuture().get();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
                for(DataBuffer d: dblist) {
                    flag.set(imageService.uploadAndValidateImages(id, d.asByteBuffer(), count.get(), list.size()));
                    count.getAndIncrement();
                    if (!flag.get()) {
                        eventDTO.setEventType(EventType.VALIDATION_FAILED);
                        eventDTO.setSuccessful(false);
                        log.info("VALIDATION FAILED");
                        eventService.save(eventDTO).subscribe();
                        return Mono.just(new ResponseEntity(false,HttpStatus.OK));
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
     *
     * @param referenceModel
     * @return
     */
    @PostMapping(value = "/reference", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> uploadReference(@Valid @RequestBody RequestVM referenceModel){

        return checkIdClient.findPerson(referenceModel.getId(), referenceModel.getDactilar())
                .flatMap(person-> {
                    imageService.uploadBase64Image(referenceModel.getId(), (String) person.get("fotografia"));
                    return Mono.just(ResponseEntity.ok().build());
                })
            .doOnError(throwable -> Mono.just(ResponseEntity.badRequest().body(throwable.getMessage())));
    }

    /**
     * Receives the image in base64 from client
     * @param referenceModel
     * @return
     */
    @PostMapping(value = "/reference-image", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity uploadReferenceImage(@org.springframework.web.bind.annotation.RequestBody RequestVM referenceModel){
        imageService.uploadBase64Image(referenceModel.getId(), referenceModel.getImage());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/principalName")
    public ResponseEntity test(Authentication authentication) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(authentication.getName());
    }
}
