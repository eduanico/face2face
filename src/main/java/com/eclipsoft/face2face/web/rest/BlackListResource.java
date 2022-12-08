package com.eclipsoft.face2face.web.rest;

import com.eclipsoft.face2face.service.BlackListService;
import com.eclipsoft.face2face.service.dto.BlackListDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class BlackListResource {

    private final BlackListService blackListService;

    public BlackListResource(BlackListService blackListService) {
        this.blackListService = blackListService;
    }

    /**
     * Save an id to a black list repository
     */
    @PostMapping(value = "/black-list")
    public Mono<ResponseEntity<Object>> addToBlackList(@RequestBody BlackListDTO blackList) {
        return blackListService.addToBlackList(blackList).flatMap(blackListDTO -> Mono.just(new ResponseEntity<>(blackListDTO, HttpStatus.OK)));
    }
}
