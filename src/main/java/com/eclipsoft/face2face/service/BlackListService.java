package com.eclipsoft.face2face.service;

import com.eclipsoft.face2face.repository.BlackListRepository;
import com.eclipsoft.face2face.service.dto.BlackListDTO;
import com.eclipsoft.face2face.service.mapper.BlackListMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BlackListService {

    private final Logger log = LoggerFactory.getLogger(BlackListService.class);


    private final BlackListRepository blackListRepository;

    private final BlackListMapper blackListMapper;

    public BlackListService(BlackListRepository blackListRepository, BlackListMapper blackListMapper) {
        this.blackListRepository = blackListRepository;
        this.blackListMapper = blackListMapper;
    }

    /**
     * Validates the id is in black list repository
     */
    public Mono<Boolean> existInBlackList(String id) {
        log.debug("EVALUATES IF IDENTIFICATION EXISTS IN BLACK LIST");
        return blackListRepository.existsBlackListByIdentificacion(id).map(aBoolean -> aBoolean);
    }

    public Mono<Object> addToBlackList(BlackListDTO blackListDTO) {
        log.debug("ADD ID TO BLACK LIST");
        String identification = blackListDTO.getIdentificacion();
        return blackListRepository.findOneByIdentificacion(identification)
                .flatMap(error -> Mono.error(new IncorrectResultSizeDataAccessException(1)))
                .switchIfEmpty(blackListRepository.save(blackListMapper.toEntity(blackListDTO)).map(blackListMapper::toDto));
    }

}
