package com.eclipsoft.face2face.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.internal.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;

public class Utils {

    @SuppressWarnings("unused")
    private final Logger log = LoggerFactory.getLogger(Utils.class);

    public static <T> T jsonToEntity(String json, Class<T> entity) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, entity);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static <T> String toJson(Object entity) {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{}";
        try {
            json = objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
   }
    public static String fileToBase64(String filePath) throws IOException {
        final byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        String file = Base64.encode(bytes);
        return file;
    }

    public static String fileBytesToBase64(byte[] fileBytes) {
        String base64 = Base64.encode(fileBytes);
        return base64;
    }

    public static String inputStreamToBase64(InputStream in) {
        try {
            return Base64.encode(in.readAllBytes());
        } catch (IOException e) {
            return "";
        }
    }

    public static InputStream stringToInputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] base64ToBytes(String base64) {
    	byte[] decodedBytes = null;
    	if(base64.trim().length() > 0) {
    		decodedBytes = java.util.Base64.getDecoder().decode(Utils.linearize(base64).getBytes());
    	}
        return decodedBytes;
    }

    public static String getValueFrom(String value, String defaultValue ) {
        return value.trim().length() > 0 ? value : defaultValue;
    }

    public static String linearize(String value) {
        return value
            .replace(" ", "")
            .replace("\n", "")
            .replace("\t", "");
    }

    public static String extractExtensionFromBase64(String base64){
        HashMap<String, String> extensions = new HashMap<>();
        extensions.put("/9j", ".jpg");
        extensions.put("iVB", ".png");
        extensions.put("Qk0", ".bmp");
        extensions.put("SUk", ".tiff");
        extensions.put("JVB", ".pdf");
        extensions.put("UEs", ".zip");
        extensions.put("MII", ".p12");
        extensions.put("UmF", ".rar");

        return extensions.get(base64.substring(0,3));
    }

    public static String extractExtensionFromBase64(String base64, String name){
        return name + Utils.extractExtensionFromBase64(base64);
    }

    public static LocalDate stringToLocalDate(String dateString, String format){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDate.parse(dateString, formatter);
    }

    public static Integer calcAge(LocalDate date){
        if( date == null) {
            return null;
        }
        Period period = Period.between(date, LocalDate.now());
        if(Objects.equals(period.getYears(), 1)){
            return period.getYears();
        }
        return period.getYears();
    }
}




















