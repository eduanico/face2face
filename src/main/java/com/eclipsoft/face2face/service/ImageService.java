package com.eclipsoft.face2face.service;

import com.eclipsoft.face2face.service.dto.EventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClient;
import software.amazon.awssdk.services.rekognition.model.*;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

@Service
public class ImageService {

    private final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final List<String> LABELS = List.of(
        "Mobile Phone",
        "Cell Phone",
        "Phone",
        "Electronics",
        "Poster",
        "Advertisement",
        "Id cards",
        "Document",
        "Blackboard"
    );

    /**
     * Uploads bytebuffer image to S3
     */
    public void uploadImageToS3(String id, String bucketName, int count, ByteBuffer imageByteBuffer) {
        S3AsyncClient client = S3AsyncClient.builder()
            .region(Region.US_EAST_2).build();
        PutObjectRequest requestS3 = PutObjectRequest.builder()
            .bucket(bucketName).key(id + "/evidencia" + count + ".jpg").build();
        client.putObject(requestS3, AsyncRequestBody.fromByteBuffer(imageByteBuffer));

    }

    /**
     * Validates rekognition labels and compare face on images.
     */
    public boolean rekognitionValidations(String id, String bucketName, ByteBuffer imageByteBuffer,
                                          EventDTO eventDTO, int count, int size,
                                          float similarityThreshold, float minConfidence) {
        boolean flag = true;

        RekognitionAsyncClient client = RekognitionAsyncClient.builder()
            .region(Region.US_EAST_2).build();

        Image souImage = Image.builder()
            .s3Object(S3Object.builder().name(id + "/reference.jpg").bucket(bucketName).build())
            .build();

        Image tarImage = Image.builder()
            .bytes(SdkBytes.fromByteBuffer(imageByteBuffer))
            .build();

        if (validateLabelsInImage(tarImage, minConfidence, client, eventDTO)) {
            eventDTO.setDetail("OK");
            if (count == 1 || count == size)
                flag = validateFaceInImage(similarityThreshold, souImage, tarImage, client, eventDTO);
        } else {
            flag = false;
        }
        log.debug("Validation with detail : {}", eventDTO.getDetail());
        return flag;
    }

    /**
     * Upload and validates the image labels and compare faces.
     */
    public boolean uploadAndValidateImages(String id, ByteBuffer imageByteBuffer,
                                           int count, int size, EventDTO eventDTO)  {
        float similarityThreshold = 90F;
        float minConfidence = 55F;
        String bucketName = "pruebas-id4face";

        uploadImageToS3(id, bucketName, count, imageByteBuffer);

        return rekognitionValidations(id, bucketName, imageByteBuffer, eventDTO,
            count, size, similarityThreshold, minConfidence);
    }

    /**
     * Validates labels on an image.
     */
    public boolean validateLabelsInImage(Image tarImage, float minConfidence,
                                         RekognitionAsyncClient rekognitionClient, EventDTO eventDTO) {
        boolean result = false;
        DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder()
            .minConfidence(minConfidence)
            .image(tarImage)
            .build();
        try {
            result = rekognitionClient.detectLabels(detectLabelsRequest).get().labels().stream().noneMatch(label -> {
                if (LABELS.contains(label.name())) {
                    log.debug(String.format("Validation failed in labels : %s", label.name()));
                    eventDTO.setDetail("Error en validación de etiqueta : " + label.name());
                }
                return LABELS.contains(label.name());
            });
        } catch (ExecutionException | InterruptedException e) {
            eventDTO.setDetail("Error de exception : " + e.getMessage());
        }
        return result;
    }

    /**
     * Validates face in image to reference face
     */
    public boolean validateFaceInImage(float similarityThreshold, Image souImage, Image tarImage,
                                       RekognitionAsyncClient rekognitionClient, EventDTO eventDTO) {
        boolean result = false;
        CompareFacesRequest request = CompareFacesRequest.builder()
            .sourceImage(souImage)
            .targetImage(tarImage)
            .similarityThreshold(similarityThreshold)
            .build();
        try {
            CompletableFuture<CompareFacesResponse> compareFacesResult = rekognitionClient.compareFaces(request);
            List<CompareFacesMatch> compareFacesMatches = compareFacesResult.get().faceMatches();
            ComparedFace face = compareFacesMatches.get(0).face();
            Float brightness = face.quality().brightness();
            Float sharpness = face.quality().sharpness();

            BoundingBox faceBoundingBox = face.boundingBox();
            float top = faceBoundingBox.top();
            float width = faceBoundingBox.width();
            float left = faceBoundingBox.left();
            float height = faceBoundingBox.height();

            if (compareFacesMatches.size() != 1) {
                eventDTO.setDetail("Error en validación de rostros, el número de rostros iguales es: "
                    + compareFacesMatches.size());
            } else if (!((top >= 0 && top <= 0.6) && (width >= 0.15 && width <= 0.35)
                && (left >= 0.20 && left <= 0.45) && (height >= 0.30 && height <= 0.6))) {
                eventDTO.setDetail("Error en bounding box.");
            } else if (brightness >= 80F) {
                result = true;
            } else if (brightness <= 50F || sharpness <= 17F) {
                eventDTO.setDetail("Error de calidad, brillo : " + brightness + ", nitidez : " + sharpness);
            } else
                result = true;

        } catch (Exception e) {
            eventDTO.setDetail("Error de exception : " + e.getMessage());
        }
        return result;
    }

    /**
     * Uploads base 64 image to S3.
     */
    public void uploadBase64ToS3(String id,  String base64Image, String bucketName) {
        S3AsyncClient client = S3AsyncClient.builder().region(Region.US_EAST_2)
            .build();
        PutObjectRequest requestS3 = PutObjectRequest.builder()
            .bucket(bucketName).key(id + "/reference.jpg").build();
        byte[] encoded = Base64.getDecoder().decode(base64Image);
        client.putObject(requestS3, AsyncRequestBody.fromBytes(encoded));
    }

}
