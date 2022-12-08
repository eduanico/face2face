package com.eclipsoft.face2face.repository;

import com.eclipsoft.face2face.domain.BlackList;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BlackListRepository extends ReactiveMongoRepository<BlackList, String> {
    Mono<Boolean> existsBlackListByIdentificacion(String identificacion);

    Mono<BlackList> findOneByIdentificacion(String identificacion);
}
