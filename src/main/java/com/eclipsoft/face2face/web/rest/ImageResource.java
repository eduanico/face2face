package com.eclipsoft.face2face.web.rest;

import com.eclipsoft.face2face.service.ImageService;
import com.eclipsoft.face2face.web.rest.vm.RequestVM;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api")
public class ImageResource {

    private final ImageService imageService;

    public ImageResource(ImageService imageService) {
        this.imageService = imageService;
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
    public Mono<ResponseEntity<Boolean>> uploadEvidences(@RequestPart("images") Flux<FilePart> images, @RequestPart("id") String id) throws ExecutionException, InterruptedException {
        AtomicInteger count = new AtomicInteger(1);
        AtomicBoolean flag = new AtomicBoolean();
        List<FilePart> list = images.collectList().toFuture().get();
        for(FilePart f: list){
            List<DataBuffer> dblist = f.content().collectList().toFuture().get();
            for(DataBuffer d: dblist){
                flag.set(imageService.uploadAndValidateImages(id, d.asByteBuffer(), count.get()));
                count.getAndIncrement();
                if(!flag.get())
                    return Mono.just(new ResponseEntity(false, HttpStatus.OK));
            }
        }
        return Mono.just(new ResponseEntity(true, HttpStatus.OK));
    }

    /**
     * Fetch the image in base64 from RCE
     * @param referenceModel
     * @return
     */
    @PostMapping(value = "/reference", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity uploadReference(@org.springframework.web.bind.annotation.RequestBody RequestVM referenceModel){
        try{
            imageService.uploadBase64Image(referenceModel.getId(), imageService.getReference(referenceModel.getId(), referenceModel.getDactilar()));
        }catch (Exception e){System.out.println(e);}
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Receives the image in base64 from client
     * @param referenceModel
     * @return
     */
    @PostMapping(value = "/reference-image", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity uploadReferenceImage(@org.springframework.web.bind.annotation.RequestBody RequestVM referenceModel){
        imageService.uploadBase64Image(referenceModel.getId(), referenceModel.getImage());
        return new ResponseEntity(HttpStatus.OK);
    }
}
