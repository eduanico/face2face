package com.eclipsoft.face2face.repository;

import com.eclipsoft.face2face.domain.Agent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

/**
 * Spring Data MongoDB reactive repository for the Agent entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ImageRepository extends ReactiveMongoRepository<MultipartFile, String> {
    Flux<Agent> findAllBy(Pageable pageable);
}
