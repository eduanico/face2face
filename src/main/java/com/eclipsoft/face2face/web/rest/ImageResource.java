package com.eclipsoft.face2face.web.rest;

import com.eclipsoft.face2face.service.ImageService;
import com.eclipsoft.face2face.web.rest.vm.RequestVM;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ImageResource {

    private final ImageService imageService;

    public ImageResource(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Validates an array of images with an id for reference
     * @param file
     * @param id
     * @return
     * @throws IOException
     */
    @PostMapping(value ="/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Boolean> uploadEvidences(@RequestParam("images") MultipartFile[] file, @RequestParam("id") String id) throws IOException {
        int count = 1;

        boolean flag;
        for (MultipartFile m : file) {
            flag = imageService.uploadAndValidateImages(id, m, count);
            if(!flag)
                return new ResponseEntity<>(false, HttpStatus.OK);
            count++;
        }
        return new ResponseEntity(true, HttpStatus.OK);
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
