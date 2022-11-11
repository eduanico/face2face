package com.eclipsoft.face2face.service;

import com.eclipsoft.face2face.Integration.CheckIdClient;
import com.eclipsoft.face2face.service.mapper.PersonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
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

@Service
public class ImageService {

    private final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final CheckIdClient checkIdClient;

    private final PersonMapper personMapper;

    private final List<String> LABELS = List.of(
        "Mobile Phone",
        "Cell Phone",
        "Phone",
        "Electronics"
    );

    public ImageService(CheckIdClient checkIdClient, PersonMapper personMapper) {
        this.checkIdClient = checkIdClient;
        this.personMapper = personMapper;
    }

    public Mono<String> getReference(String identification, String dactilar) {
        return checkIdClient
            .findPerson(identification, dactilar).map(personMapper::toDto)
            .map(personDTO -> personDTO.getFotografia());
    }


    public boolean uploadAndValidateImages(String id, ByteBuffer imageByteBuffer, int count, int size)  {
//        AwsCredentialsProvider credentialsProvider = ProfileCredentialsProvider.builder().profileName("default").build();
        float similarityThreshold = 90F;
        float minConfidence = 80F;
        boolean flag;


        S3AsyncClient client = S3AsyncClient.builder()
//            .credentialsProvider(credentialsProvider)
            .region(Region.US_EAST_2).build();

        RekognitionAsyncClient rekognitionClient = RekognitionAsyncClient.builder()
//            .credentialsProvider(credentialsProvider)
            .region(Region.US_EAST_2).build();

        PutObjectRequest requestS3 = PutObjectRequest.builder()
            .bucket("pruebas-id4face").key(id+"/evidencia" + count + ".jpg").build();
        client.putObject(requestS3, AsyncRequestBody.fromByteBuffer(imageByteBuffer));

        Image souImage = Image.builder()
            .s3Object(S3Object.builder().name(id+"/reference.jpg").bucket("pruebas-id4face").build())
            .build();

        Image tarImage = Image.builder()
            .bytes(SdkBytes.fromByteBuffer(imageByteBuffer))
            .build();

        if(validateLabelsInImage(tarImage, minConfidence, rekognitionClient)) {
            flag = true;
            if( count == 1 || count == size)
                flag = validateFaceInImage(similarityThreshold, souImage, tarImage, rekognitionClient);
        }else {
            flag = false;
        }
        return flag;
    }

    public boolean validateLabelsInImage(Image tarImage, float similarityThreshold, RekognitionAsyncClient rekognitionClient){
        DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder()
            .minConfidence(similarityThreshold)
            .image(tarImage)
            .build();
        try {
            return !rekognitionClient.detectLabels(detectLabelsRequest).get().labels().stream().anyMatch(LABELS::contains);
        }catch(Exception e){
            return false;
        }
    }


    public boolean validateFaceInImage(float similarityThreshold, Image souImage, Image tarImage, RekognitionAsyncClient rekognitionClient) {
        CompareFacesRequest request = CompareFacesRequest.builder()
            .sourceImage(souImage)
            .targetImage(tarImage)
            .similarityThreshold(similarityThreshold)
            .build();

        try {
            CompletableFuture<CompareFacesResponse> compareFacesResult = rekognitionClient.compareFaces(request);
            List<CompareFacesMatch> compareFacesMatches = compareFacesResult.get().faceMatches();
            if( compareFacesMatches.size() >= 2 || compareFacesMatches.size() == 0){
                return false;
            }
            BoundingBox faceBoundingBox = compareFacesMatches.get(0).face().boundingBox();
            float top = faceBoundingBox.top();
            float width = faceBoundingBox.width();
            float left = faceBoundingBox.left();
            float height = faceBoundingBox.height();
            if(!((top >= 0.2 && top <= 0.4) && (width >= 0.15 && width <= 0.3) &&
                (left >= 0.35 && left <= 0.45) && (height >= 0.40 && height <= 0.50)))
                return false;
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
            //            CompletableFuture<DetectFacesResponse> detectFacesResponse =
//            rekognitionClient.detectFaces(detectFacesRequest);
//            log.info(detectFacesResponse.get().toString());
//            List<FaceDetail> faceDetails = detectFacesResponse.get()
//                .faceDetails();
//            for(FaceDetail faceDetail: faceDetails)
//                log.info(faceDetail.toString());
//            log.info("\nLas etiquetas son: " + la);
//                if(LABELS.contains(la.name()))
//                    return false;
//                if (la.instances().get(0) != null)
//                    log.info("\nLa instancia es : " + String.valueOf(la.instances().get(0)));
//            List<Label> labels = detectLabelsResponse.get().labels();
//            for (CompareFacesMatch match : faceDetails) {
//                ComparedFace face = match.face();
//                if (face.confidence() < 90) {
//                    return false;
//                }
//                return true;
//            }
//            rekognitionClient.close();
        }catch(Exception e){
            return false;
        }
        return false;
    }

    public void uploadBase64Image(String id,  String reference){
        S3AsyncClient client = S3AsyncClient.builder().region(Region.US_EAST_2)
//            .credentialsProvider(credentialsProvider)
            .build();
        PutObjectRequest requestS3 = PutObjectRequest.builder()
            .bucket("pruebas-id4face").key(id+"/reference.jpg").build();
        byte[] encoded = Base64.getDecoder().decode(reference);
        client.putObject(requestS3, AsyncRequestBody.fromBytes(encoded));
    }

}
