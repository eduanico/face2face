package com.eclipsoft.face2face.Integration;

import com.eclipsoft.face2face.Exception.CheckIdBadRequestException;
import com.eclipsoft.face2face.Exception.CheckIdIntegrationException;
import com.eclipsoft.face2face.config.ApplicationProperties;
import com.eclipsoft.face2face.config.properties.CheckId;
import com.eclipsoft.face2face.service.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import io.netty.channel.ChannelOption;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Component
public class CheckIdClient {

    private final Logger log = LoggerFactory.getLogger(CheckIdClient.class);

    private final WebClient webClient;

    private static final int CONNECT_TIMEOUT_IN_SECONDS = 10;

    private final CheckId checkIdProperties;

    public CheckIdClient(ApplicationProperties applicationProperties) {
        this.checkIdProperties = applicationProperties.getIntegration().getCheckId();
        this.webClient = this.buildWebClient();
    }

    public void connectionValid() throws RuntimeException {
        webClient
                .get()
                .uri("/management/health")
                .retrieve()
                .onStatus(
                        HttpStatus::is4xxClientError,
                        response -> {
                            throw new RuntimeException();
                        }
                )
                .onStatus(
                        HttpStatus::is5xxServerError,
                        response -> {
                            throw new RuntimeException();
                        }
                )
                .bodyToMono(String.class)
                .onErrorMap(ConnectTimeoutException.class, connectTimeoutException -> new Exception(connectTimeoutException.getMessage()))
                .toFuture();
    }

    private WebClient buildWebClient() {
        int readAndWriteTimeout = this.checkIdProperties.getTimeoutInSeconds() * 1000;

        HttpClient httpClient = HttpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_IN_SECONDS * 1000)
                .responseTimeout(Duration.ofMillis(readAndWriteTimeout))
                .doOnConnected(
                        conn ->
                                conn
                                        .addHandlerLast(new ReadTimeoutHandler(readAndWriteTimeout, TimeUnit.MILLISECONDS))
                                        .addHandlerLast(new WriteTimeoutHandler(readAndWriteTimeout, TimeUnit.MILLISECONDS))
                );

        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        return WebClient
                .builder()
                .baseUrl(this.checkIdProperties.getBaseUrl())
                .clientConnector(connector)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<Map<String, Object>> findPerson(String identification, String dactilar) {
        log.debug("Request to findPerson: {}, {}", identification, dactilar);

        return this.authenticate()
                .flatMap(auth -> {
                    String jwt = this.authenticateToJwt(auth);
                    return webClient
                            .post()
                            .uri("/api/find-person-biometric-data")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, jwt)
                            .acceptCharset(StandardCharsets.UTF_8)
                            .body(BodyInserters.fromValue(
                                    Map.of("identificacion", identification, "dactilar", dactilar)
                            ))
                            .retrieve()
                            .onStatus(Predicate.not(HttpStatus::is2xxSuccessful),
                                    clientResponse ->
                                            clientResponse.bodyToMono(JsonNode.class)
                                                    .flatMap(jsonNode -> {

                                                        String errorMessage = "";
                                                        if (jsonNode.has("detail")) {
                                                            errorMessage = jsonNode.get("detail").asText("");
                                                        }

                                                        if (clientResponse.statusCode().is4xxClientError()) {
                                                            if (errorMessage.isEmpty()) {
                                                                throw new CheckIdBadRequestException();
                                                            }
                                                            throw new CheckIdBadRequestException(errorMessage);
                                                        }
                                                        throw new CheckIdIntegrationException("CheckId sent an unexpected response related to server error. " + errorMessage);
                                                    })
                            )
                            .bodyToMono(new ParameterizedTypeReference<>() {
                            });
                });
    }

    public Mono<Map<String, Object>> findPersonDemographicData(String identification) {
        log.debug("Request to findPersonDemographicData: {}", identification);

        return this.authenticate()
                .flatMap(auth -> {
                    String jwt = this.authenticateToJwt(auth);
                    return webClient
                            .post()
                            .uri("/api/find-person-demographic-data")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, jwt)
                            .acceptCharset(StandardCharsets.UTF_8)
                            .body(BodyInserters.fromValue(
                                    Map.of("identificacion", identification)
                            ))
                            .retrieve()
                            .onStatus(Predicate.not(HttpStatus::is2xxSuccessful),
                                    clientResponse ->
                                            clientResponse.bodyToMono(JsonNode.class)
                                                    .flatMap(jsonNode -> {

                                                        String errorMessage = "";
                                                        if (jsonNode.has("detail")) {
                                                            errorMessage = jsonNode.get("detail").asText("");
                                                        }

                                                        if (clientResponse.statusCode().is4xxClientError()) {
                                                            if (errorMessage.isEmpty()) {
                                                                return Mono.error(new CheckIdBadRequestException());
                                                            }
                                                            return Mono.error(new CheckIdBadRequestException(errorMessage));
                                                        }
                                                        return Mono.error(new CheckIdIntegrationException("CheckId sent an unexpected response related to server error. " + errorMessage));
                                                    })
                            )
                            .bodyToMono(new ParameterizedTypeReference<>() {
                            });
                });
    }

    public Mono<Map<String, Object>> findPersonBiometricDataFromCRCG(String identification) {
        log.debug("Request to findPersonBiometricDataFromCRCG: {}", identification);

        return this.authenticate()
                .flatMap(auth -> {
                    String jwt = this.authenticateToJwt(auth);
                    return webClient
                            .post()
                            .uri("/api/crcg/find-person-biometric-data")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, jwt)
                            .acceptCharset(StandardCharsets.UTF_8)
                            .body(BodyInserters.fromValue(
                                    Map.of("identificacion", identification)
                            ))
                            .retrieve()
                            .onStatus(Predicate.not(HttpStatus::is2xxSuccessful),
                                    clientResponse ->
                                            clientResponse.bodyToMono(JsonNode.class)
                                                    .flatMap(jsonNode -> {

                                                        String errorMessage = "";
                                                        if (jsonNode.has("detail")) {
                                                            errorMessage = jsonNode.get("detail").asText("");
                                                        }

                                                        if (clientResponse.statusCode().is4xxClientError()) {
                                                            if (errorMessage.isEmpty()) {
                                                                return Mono.error(new CheckIdBadRequestException());
                                                            }
                                                            return Mono.error(new CheckIdBadRequestException(errorMessage));
                                                        }
                                                        return Mono.error(new CheckIdIntegrationException("CheckId sent an unexpected response related to server error. " + errorMessage));
                                                    })
                            )
                            .bodyToMono(new ParameterizedTypeReference<>() {
                            });
                });
    }

    public Mono<Map<String, Object>> findLocalPersonBiometricData(String identification) {
        log.debug("Request to findLocalPersonBiometricData: {}", identification);

        return this.authenticate()
                .flatMap(auth -> {

                    String jwt = this.authenticateToJwt(auth);

                    return webClient
                            .post()
                            .uri("/api/local/find-person-biometric-data")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, jwt)
                            .acceptCharset(StandardCharsets.UTF_8)
                            .body(BodyInserters.fromValue(
                                    Map.of("identificacion", identification)
                            ))
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<>() {
                            });
                });
    }

    private Mono<String> authenticate() {
        log.debug("Request to authenticate");

        return webClient
                .post()
                .uri("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .body(
                        BodyInserters.fromValue(
                                Map.of("username", checkIdProperties.getUsername(), "password", checkIdProperties.getPassword())
                        )
                )
                .retrieve()
                .bodyToMono(String.class);
    }

    private String authenticateToJwt(String jsonResponse) {
        log.debug("REST Request authenticateToJwt: {}", jsonResponse);

        JsonNode res = Utils.jsonToEntity(jsonResponse, JsonNode.class);
        assert res != null;
        return "Bearer " + res.get("id_token").asText();
    }
}
