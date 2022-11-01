package com.eclipsoft.face2face.service;

import com.eclipsoft.face2face.Integration.CheckIdClient;
import com.eclipsoft.face2face.repository.ImageRepository;
import com.eclipsoft.face2face.service.dto.EventDTO;
import com.eclipsoft.face2face.service.dto.PersonDTO;
import com.eclipsoft.face2face.service.mapper.PersonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClient;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ImageService {

    private final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final ImageRepository imageRepository;

    private final CheckIdClient checkIdClient;

    private final PersonMapper personMapper;

    private final List<String> LABELS = List.of(
        "Mobile Phone",
        "Cell Phone",
        "Phone",
        "Electronics"
    );


    private final EventService eventService;

    public ImageService(ImageRepository imageRepository, CheckIdClient checkIdClient, PersonMapper personMapper, EventService eventService) {
        this.imageRepository = imageRepository;
        this.checkIdClient = checkIdClient;
        this.personMapper = personMapper;
        this.eventService = eventService;
    }

    public String getReference(String identification, String dactilar) throws ExecutionException, InterruptedException {
        return checkIdClient
            .findPerson(identification, dactilar)
            .map(persona -> {
                PersonDTO person = personMapper.toDto(persona);
                return person.getFotografia();
            }).block();
    }


    public boolean uploadAndValidateImages(String id, ByteBuffer imageByteBuffer, int count)  {
        AwsCredentialsProvider credentialsProvider = ProfileCredentialsProvider.builder().profileName("S3").build();

        S3AsyncClient client = S3AsyncClient.builder().region(Region.US_EAST_2).credentialsProvider(credentialsProvider).build();

        PutObjectRequest requestS3 = PutObjectRequest.builder()
            .bucket("pruebas-id4face").key(id+"/evidencia" + count + ".jpg").build();
        client.putObject(requestS3, AsyncRequestBody.fromByteBuffer(imageByteBuffer));
        if(validateFaceInImage(id, imageByteBuffer)) {
//            imageRepository.save(imageFile);
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
//        client.putObject(requestS3, AsyncRequestBody.fromBytes(encoded));
        client.putObject(requestS3,
            AsyncRequestBody.fromBytes(encoded)
        );
    }


    public boolean validateFaceInImage(String id, ByteBuffer imageByteBuffer) {
        float similarityThreshold = 90F;

        Image souImage = Image.builder()
            .s3Object(S3Object.builder().name(id+"/reference.jpg").bucket("pruebas-id4face").build())
            .build();

        Image tarImage = Image.builder()
            .bytes(SdkBytes.fromByteBuffer(imageByteBuffer))
            .build();

        CompareFacesRequest request = CompareFacesRequest.builder()
            .sourceImage(souImage)
            .targetImage(tarImage)
            .similarityThreshold(similarityThreshold)
            .build();

        DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder().minConfidence(80F).image(tarImage).build();

        AwsCredentialsProvider credentialsProvider = ProfileCredentialsProvider.builder().profileName("default").build();

//        DetectFacesRequest detectFacesRequest = DetectFacesRequest.builder().image(tarImage).attributes(Attribute.ALL).build();

        RekognitionAsyncClient rekognitionClient = RekognitionAsyncClient.builder().credentialsProvider(credentialsProvider)
            .region(Region.US_EAST_2).build();



        try {
            CompletableFuture<DetectLabelsResponse> detectLabelsResponse = rekognitionClient.detectLabels(detectLabelsRequest);
            List<Label> labels = detectLabelsResponse.get().labels();


//            CompletableFuture<DetectFacesResponse> detectFacesResponse =
//                rekognitionClient.detectFaces(detectFacesRequest);
//
//
//            log.info(detectFacesResponse.get().toString());
//            List<FaceDetail> faceDetails = detectFacesResponse.get()
//                .faceDetails();
//            for(FaceDetail faceDetail: faceDetails)
//                log.info(faceDetail.toString());
            for(Label la : labels) {
                if(la.instances().size()>=2)
                    return false;
//                log.info("\nLas etiquetas son: " + la);
                if(LABELS.contains(la.name()))
                    return false;

//                if (la.instances().get(0) != null)
//                    log.info("\nLa instancia es : " + String.valueOf(la.instances().get(0)));
            }
//            List<Label> labels = detectLabelsResponse.get().labels();

            CompletableFuture<CompareFacesResponse> compareFacesResult = rekognitionClient.compareFaces(request);
            List<CompareFacesMatch> compareFacesMatches = compareFacesResult.get().faceMatches();

            if(compareFacesResult.get().unmatchedFaces().size() >= 1 || compareFacesMatches.size() >= 2){
                return false;
            }


//            for (CompareFacesMatch match : faceDetails) {
//                ComparedFace face = match.face();
//                if (face.confidence() < 90) {
//                    return false;
//                }
//                return true;
//            }
//            rekognitionClient.close();
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
