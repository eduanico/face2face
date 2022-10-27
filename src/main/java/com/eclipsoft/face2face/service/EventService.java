package com.eclipsoft.face2face.service;

import com.eclipsoft.face2face.domain.Event;
import com.eclipsoft.face2face.repository.EventRepository;
import com.eclipsoft.face2face.service.dto.EventDTO;
import com.eclipsoft.face2face.service.mapper.EventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Event}.
 */
@Service
public class EventService {

    private final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;

    private final EventMapper eventMapper;

    public EventService(EventRepository eventRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }

    /**
     * Save a event.
     *
     * @param eventDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<EventDTO> save(EventDTO eventDTO) {
        log.debug("Request to save Event : {}", eventDTO);
        return eventRepository.save(eventMapper.toEntity(eventDTO)).map(eventMapper::toDto);
    }

    /**
     * Update a event.
     *
     * @param eventDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<EventDTO> update(EventDTO eventDTO) {
        log.debug("Request to update Event : {}", eventDTO);
        return eventRepository.save(eventMapper.toEntity(eventDTO)).map(eventMapper::toDto);
    }

    /**
     * Partially update a event.
     *
     * @param eventDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<EventDTO> partialUpdate(EventDTO eventDTO) {
        log.debug("Request to partially update Event : {}", eventDTO);

        return eventRepository
            .findById(eventDTO.getId())
            .map(existingEvent -> {
                eventMapper.partialUpdate(existingEvent, eventDTO);

                return existingEvent;
            })
            .flatMap(eventRepository::save)
            .map(eventMapper::toDto);
    }

    /**
     * Get all the events.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    public Flux<EventDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Events");
        return eventRepository.findAllBy(pageable).map(eventMapper::toDto);
    }

    /**
     * Returns the number of events available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return eventRepository.count();
    }

    /**
     * Get one event by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Mono<EventDTO> findOne(String id) {
        log.debug("Request to get Event : {}", id);
        return eventRepository.findById(id).map(eventMapper::toDto);
    }

    /**
     * Delete the event by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Event : {}", id);
        return eventRepository.deleteById(id);
    }
}
