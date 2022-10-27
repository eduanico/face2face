package com.eclipsoft.face2face.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.eclipsoft.face2face.IntegrationTest;
import com.eclipsoft.face2face.domain.Event;
import com.eclipsoft.face2face.domain.enumeration.EventType;
import com.eclipsoft.face2face.repository.EventRepository;
import com.eclipsoft.face2face.service.dto.EventDTO;
import com.eclipsoft.face2face.service.mapper.EventMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link EventResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class EventResourceIT {

    private static final String DEFAULT_IDENTIFICATION = "AAAAAAAAAA";
    private static final String UPDATED_IDENTIFICATION = "BBBBBBBBBB";

    private static final String DEFAULT_DACTILAR = "AAAAAAAAAA";
    private static final String UPDATED_DACTILAR = "BBBBBBBBBB";

    private static final Instant DEFAULT_VALIDATION_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_VALIDATION_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Boolean DEFAULT_SUCCESSFUL = false;
    private static final Boolean UPDATED_SUCCESSFUL = true;

    private static final EventType DEFAULT_EVENT_TYPE = EventType.VALIDATION_SUCCESS;
    private static final EventType UPDATED_EVENT_TYPE = EventType.VALIDATION_FAILED;

    private static final String ENTITY_API_URL = "/api/events";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private WebTestClient webTestClient;

    private Event event;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Event createEntity() {
        Event event = new Event()
            .identification(DEFAULT_IDENTIFICATION)
            .dactilar(DEFAULT_DACTILAR)
            .validationDate(DEFAULT_VALIDATION_DATE)
            .successful(DEFAULT_SUCCESSFUL)
            .eventType(DEFAULT_EVENT_TYPE);
        return event;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Event createUpdatedEntity() {
        Event event = new Event()
            .identification(UPDATED_IDENTIFICATION)
            .dactilar(UPDATED_DACTILAR)
            .validationDate(UPDATED_VALIDATION_DATE)
            .successful(UPDATED_SUCCESSFUL)
            .eventType(UPDATED_EVENT_TYPE);
        return event;
    }

    @BeforeEach
    public void initTest() {
        eventRepository.deleteAll().block();
        event = createEntity();
    }

    @Test
    void createEvent() throws Exception {
        int databaseSizeBeforeCreate = eventRepository.findAll().collectList().block().size();
        // Create the Event
        EventDTO eventDTO = eventMapper.toDto(event);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll().collectList().block();
        assertThat(eventList).hasSize(databaseSizeBeforeCreate + 1);
        Event testEvent = eventList.get(eventList.size() - 1);
        assertThat(testEvent.getIdentification()).isEqualTo(DEFAULT_IDENTIFICATION);
        assertThat(testEvent.getDactilar()).isEqualTo(DEFAULT_DACTILAR);
        assertThat(testEvent.getValidationDate()).isEqualTo(DEFAULT_VALIDATION_DATE);
        assertThat(testEvent.getSuccessful()).isEqualTo(DEFAULT_SUCCESSFUL);
        assertThat(testEvent.getEventType()).isEqualTo(DEFAULT_EVENT_TYPE);
    }

    @Test
    void createEventWithExistingId() throws Exception {
        // Create the Event with an existing ID
        event.setId("existing_id");
        EventDTO eventDTO = eventMapper.toDto(event);

        int databaseSizeBeforeCreate = eventRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll().collectList().block();
        assertThat(eventList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void getAllEvents() {
        // Initialize the database
        eventRepository.save(event).block();

        // Get all the eventList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(event.getId()))
            .jsonPath("$.[*].identification")
            .value(hasItem(DEFAULT_IDENTIFICATION))
            .jsonPath("$.[*].dactilar")
            .value(hasItem(DEFAULT_DACTILAR))
            .jsonPath("$.[*].validationDate")
            .value(hasItem(DEFAULT_VALIDATION_DATE.toString()))
            .jsonPath("$.[*].successful")
            .value(hasItem(DEFAULT_SUCCESSFUL.booleanValue()))
            .jsonPath("$.[*].eventType")
            .value(hasItem(DEFAULT_EVENT_TYPE.toString()));
    }

    @Test
    void getEvent() {
        // Initialize the database
        eventRepository.save(event).block();

        // Get the event
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, event.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(event.getId()))
            .jsonPath("$.identification")
            .value(is(DEFAULT_IDENTIFICATION))
            .jsonPath("$.dactilar")
            .value(is(DEFAULT_DACTILAR))
            .jsonPath("$.validationDate")
            .value(is(DEFAULT_VALIDATION_DATE.toString()))
            .jsonPath("$.successful")
            .value(is(DEFAULT_SUCCESSFUL.booleanValue()))
            .jsonPath("$.eventType")
            .value(is(DEFAULT_EVENT_TYPE.toString()));
    }

    @Test
    void getNonExistingEvent() {
        // Get the event
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingEvent() throws Exception {
        // Initialize the database
        eventRepository.save(event).block();

        int databaseSizeBeforeUpdate = eventRepository.findAll().collectList().block().size();

        // Update the event
        Event updatedEvent = eventRepository.findById(event.getId()).block();
        updatedEvent
            .identification(UPDATED_IDENTIFICATION)
            .dactilar(UPDATED_DACTILAR)
            .validationDate(UPDATED_VALIDATION_DATE)
            .successful(UPDATED_SUCCESSFUL)
            .eventType(UPDATED_EVENT_TYPE);
        EventDTO eventDTO = eventMapper.toDto(updatedEvent);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, eventDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll().collectList().block();
        assertThat(eventList).hasSize(databaseSizeBeforeUpdate);
        Event testEvent = eventList.get(eventList.size() - 1);
        assertThat(testEvent.getIdentification()).isEqualTo(UPDATED_IDENTIFICATION);
        assertThat(testEvent.getDactilar()).isEqualTo(UPDATED_DACTILAR);
        assertThat(testEvent.getValidationDate()).isEqualTo(UPDATED_VALIDATION_DATE);
        assertThat(testEvent.getSuccessful()).isEqualTo(UPDATED_SUCCESSFUL);
        assertThat(testEvent.getEventType()).isEqualTo(UPDATED_EVENT_TYPE);
    }

    @Test
    void putNonExistingEvent() throws Exception {
        int databaseSizeBeforeUpdate = eventRepository.findAll().collectList().block().size();
        event.setId(UUID.randomUUID().toString());

        // Create the Event
        EventDTO eventDTO = eventMapper.toDto(event);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, eventDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll().collectList().block();
        assertThat(eventList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchEvent() throws Exception {
        int databaseSizeBeforeUpdate = eventRepository.findAll().collectList().block().size();
        event.setId(UUID.randomUUID().toString());

        // Create the Event
        EventDTO eventDTO = eventMapper.toDto(event);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll().collectList().block();
        assertThat(eventList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamEvent() throws Exception {
        int databaseSizeBeforeUpdate = eventRepository.findAll().collectList().block().size();
        event.setId(UUID.randomUUID().toString());

        // Create the Event
        EventDTO eventDTO = eventMapper.toDto(event);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll().collectList().block();
        assertThat(eventList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateEventWithPatch() throws Exception {
        // Initialize the database
        eventRepository.save(event).block();

        int databaseSizeBeforeUpdate = eventRepository.findAll().collectList().block().size();

        // Update the event using partial update
        Event partialUpdatedEvent = new Event();
        partialUpdatedEvent.setId(event.getId());

        partialUpdatedEvent.dactilar(UPDATED_DACTILAR);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedEvent.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedEvent))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll().collectList().block();
        assertThat(eventList).hasSize(databaseSizeBeforeUpdate);
        Event testEvent = eventList.get(eventList.size() - 1);
        assertThat(testEvent.getIdentification()).isEqualTo(DEFAULT_IDENTIFICATION);
        assertThat(testEvent.getDactilar()).isEqualTo(UPDATED_DACTILAR);
        assertThat(testEvent.getValidationDate()).isEqualTo(DEFAULT_VALIDATION_DATE);
        assertThat(testEvent.getSuccessful()).isEqualTo(DEFAULT_SUCCESSFUL);
        assertThat(testEvent.getEventType()).isEqualTo(DEFAULT_EVENT_TYPE);
    }

    @Test
    void fullUpdateEventWithPatch() throws Exception {
        // Initialize the database
        eventRepository.save(event).block();

        int databaseSizeBeforeUpdate = eventRepository.findAll().collectList().block().size();

        // Update the event using partial update
        Event partialUpdatedEvent = new Event();
        partialUpdatedEvent.setId(event.getId());

        partialUpdatedEvent
            .identification(UPDATED_IDENTIFICATION)
            .dactilar(UPDATED_DACTILAR)
            .validationDate(UPDATED_VALIDATION_DATE)
            .successful(UPDATED_SUCCESSFUL)
            .eventType(UPDATED_EVENT_TYPE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedEvent.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedEvent))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll().collectList().block();
        assertThat(eventList).hasSize(databaseSizeBeforeUpdate);
        Event testEvent = eventList.get(eventList.size() - 1);
        assertThat(testEvent.getIdentification()).isEqualTo(UPDATED_IDENTIFICATION);
        assertThat(testEvent.getDactilar()).isEqualTo(UPDATED_DACTILAR);
        assertThat(testEvent.getValidationDate()).isEqualTo(UPDATED_VALIDATION_DATE);
        assertThat(testEvent.getSuccessful()).isEqualTo(UPDATED_SUCCESSFUL);
        assertThat(testEvent.getEventType()).isEqualTo(UPDATED_EVENT_TYPE);
    }

    @Test
    void patchNonExistingEvent() throws Exception {
        int databaseSizeBeforeUpdate = eventRepository.findAll().collectList().block().size();
        event.setId(UUID.randomUUID().toString());

        // Create the Event
        EventDTO eventDTO = eventMapper.toDto(event);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, eventDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll().collectList().block();
        assertThat(eventList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchEvent() throws Exception {
        int databaseSizeBeforeUpdate = eventRepository.findAll().collectList().block().size();
        event.setId(UUID.randomUUID().toString());

        // Create the Event
        EventDTO eventDTO = eventMapper.toDto(event);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll().collectList().block();
        assertThat(eventList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamEvent() throws Exception {
        int databaseSizeBeforeUpdate = eventRepository.findAll().collectList().block().size();
        event.setId(UUID.randomUUID().toString());

        // Create the Event
        EventDTO eventDTO = eventMapper.toDto(event);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll().collectList().block();
        assertThat(eventList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteEvent() {
        // Initialize the database
        eventRepository.save(event).block();

        int databaseSizeBeforeDelete = eventRepository.findAll().collectList().block().size();

        // Delete the event
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, event.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Event> eventList = eventRepository.findAll().collectList().block();
        assertThat(eventList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
