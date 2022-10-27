package com.eclipsoft.face2face.service;

import com.eclipsoft.face2face.domain.Agent;
import com.eclipsoft.face2face.repository.AgentRepository;
import com.eclipsoft.face2face.service.dto.AgentDTO;
import com.eclipsoft.face2face.service.mapper.AgentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Agent}.
 */
@Service
public class AgentService {

    private final Logger log = LoggerFactory.getLogger(AgentService.class);

    private final AgentRepository agentRepository;

    private final AgentMapper agentMapper;

    public AgentService(AgentRepository agentRepository, AgentMapper agentMapper) {
        this.agentRepository = agentRepository;
        this.agentMapper = agentMapper;
    }

    /**
     * Save a agent.
     *
     * @param agentDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<AgentDTO> save(AgentDTO agentDTO) {
        log.debug("Request to save Agent : {}", agentDTO);
        return agentRepository.save(agentMapper.toEntity(agentDTO)).map(agentMapper::toDto);
    }

    /**
     * Update a agent.
     *
     * @param agentDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<AgentDTO> update(AgentDTO agentDTO) {
        log.debug("Request to update Agent : {}", agentDTO);
        return agentRepository.save(agentMapper.toEntity(agentDTO)).map(agentMapper::toDto);
    }

    /**
     * Partially update a agent.
     *
     * @param agentDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<AgentDTO> partialUpdate(AgentDTO agentDTO) {
        log.debug("Request to partially update Agent : {}", agentDTO);

        return agentRepository
            .findById(agentDTO.getId())
            .map(existingAgent -> {
                agentMapper.partialUpdate(existingAgent, agentDTO);

                return existingAgent;
            })
            .flatMap(agentRepository::save)
            .map(agentMapper::toDto);
    }

    /**
     * Get all the agents.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    public Flux<AgentDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Agents");
        return agentRepository.findAllBy(pageable).map(agentMapper::toDto);
    }

    /**
     * Returns the number of agents available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return agentRepository.count();
    }

    /**
     * Get one agent by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Mono<AgentDTO> findOne(String id) {
        log.debug("Request to get Agent : {}", id);
        return agentRepository.findById(id).map(agentMapper::toDto);
    }

    /**
     * Delete the agent by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Agent : {}", id);
        return agentRepository.deleteById(id);
    }
}
