package com.eclipsoft.face2face.service;

import com.eclipsoft.face2face.Integration.CheckIdClient;
import com.eclipsoft.face2face.repository.ImageRepository;
import com.eclipsoft.face2face.service.dto.PersonDTO;
import com.eclipsoft.face2face.service.mapper.PersonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClient;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ImageService {

    private final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final ImageRepository imageRepository;

    private final CheckIdClient checkIdClient;

    private final PersonMapper personMapper;


    public ImageService(ImageRepository imageRepository, CheckIdClient checkIdClient, PersonMapper personMapper) {
        this.imageRepository = imageRepository;
        this.checkIdClient = checkIdClient;
        this.personMapper = personMapper;
    }

    public String getReference(String identification, String dactilar) throws ExecutionException, InterruptedException {
        return checkIdClient
            .findPerson(identification, dactilar)
            .map(persona -> {
                PersonDTO person = personMapper.toDto(persona);
                return person.getFotografia();
            }).toFuture().get();
    }


    public boolean uploadAndValidateImages(String id, MultipartFile imageFile, int count) throws IOException {
        S3AsyncClient client = S3AsyncClient.builder().region(Region.US_EAST_2).build();

        PutObjectRequest requestS3 = PutObjectRequest.builder()
            .bucket("pruebas-id4face").key(id+"/evidencia" + count + ".jpg").build();
        client.putObject(requestS3, AsyncRequestBody.fromBytes(imageFile.getBytes()));
        if(validateFaceInImage(id, imageFile)) {
            imageRepository.save(imageFile);
            return true;
        }else {
            return false;
        }
    }

    public void uploadBase64Image(String id,  String reference){
        S3AsyncClient client = S3AsyncClient.builder().region(Region.US_EAST_2).build();
        PutObjectRequest requestS3 = PutObjectRequest.builder()
            .bucket("pruebas-id4face").key(id+"/reference.jpg").build();
        byte[] encoded = Base64.getDecoder().decode(reference);
        client.putObject(requestS3,
            AsyncRequestBody.fromBytes(encoded)
        );
    }


    public boolean validateFaceInImage(String id, MultipartFile imageFile) throws IOException {
        float similarityThreshold = 90F;

        Image souImage = Image.builder()
            .s3Object(S3Object.builder().name(id+"/reference.jpg").bucket("pruebas-id4face").build())
            .build();

        Image tarImage = Image.builder()
            .bytes(SdkBytes.fromByteArray(imageFile.getBytes()))
            .build();

        CompareFacesRequest request = CompareFacesRequest.builder()
            .sourceImage(souImage)
            .targetImage(tarImage)
            .similarityThreshold(similarityThreshold)
            .build();

        DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder().image(tarImage).build();

        RekognitionAsyncClient rekognitionClient = RekognitionAsyncClient.builder()
            .region(Region.US_EAST_2).build();

        try {
//            CompletableFuture<DetectLabelsResponse> detectLabelsResponse = rekognitionClient.detectLabels(detectLabelsRequest);
//            List<Label> labels = detectLabelsResponse.labels();
//            for(Label la : labels) {
//                if (la.instances().get(0) != null)
//                    System.out.printf(String.valueOf(la.instances().get(0)));
//            }
//            List<Label> labels = detectLabelsResponse.get().labels();
            CompletableFuture<CompareFacesResponse> compareFacesResult = rekognitionClient.compareFaces(request);
            List<CompareFacesMatch> faceDetails = compareFacesResult.get().faceMatches();
            if(compareFacesResult.get().unmatchedFaces().size() >= 1 ){
                return false;
            }
//            for (CompareFacesMatch match : faceDetails) {
//                ComparedFace face = match.face();
//                if (face.confidence() < 90) {
//                    return false;
//                }
//                return true;
//            }
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public boolean analyzeFaceInImages(String id, int count){
        float similarityThreshold = 90F;

        Image souImage = Image.builder()
            .s3Object(S3Object.builder().name(id+"/reference.jpg").bucket("pruebas-id4face").build())
            .build();

        Image tarImage = Image.builder()
            .s3Object(S3Object.builder().name(id+"/evidencia"+count+".jpg").bucket("pruebas-id4face").build())
            .build();

        CompareFacesRequest request = CompareFacesRequest.builder()
            .sourceImage(souImage)
            .targetImage(tarImage)
            .similarityThreshold(similarityThreshold)
            .build();

        RekognitionClient rekognitionClient = RekognitionClient.builder()
            .region(Region.US_EAST_2).build();

        try {
            CompareFacesResponse compareFacesResult = rekognitionClient.compareFaces(request);
            List<CompareFacesMatch> faceDetails = compareFacesResult.faceMatches();

            if(compareFacesResult.unmatchedFaces().size() > 1 || compareFacesResult.unmatchedFaces().size() == 1 ){
                return false;
            }
            for (CompareFacesMatch match : faceDetails) {
                ComparedFace face = match.face();
                if (face.confidence() < 90) {
                    return false;
                }
                return true;
            }
        }catch(Exception e){
            return false;
        }
        return false;
    }

}
