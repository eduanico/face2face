package com.eclipsoft.face2face.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.eclipsoft.face2face.IntegrationTest;
import com.eclipsoft.face2face.domain.Agent;
import com.eclipsoft.face2face.repository.AgentRepository;
import com.eclipsoft.face2face.service.dto.AgentDTO;
import com.eclipsoft.face2face.service.mapper.AgentMapper;
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
 * Integration tests for the {@link AgentResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class AgentResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final Instant DEFAULT_REGISTRATION_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_REGISTRATION_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/agents";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private WebTestClient webTestClient;

    private Agent agent;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Agent createEntity() {
        Agent agent = new Agent()
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .active(DEFAULT_ACTIVE)
            .registrationDate(DEFAULT_REGISTRATION_DATE);
        return agent;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Agent createUpdatedEntity() {
        Agent agent = new Agent()
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .active(UPDATED_ACTIVE)
            .registrationDate(UPDATED_REGISTRATION_DATE);
        return agent;
    }

    @BeforeEach
    public void initTest() {
        agentRepository.deleteAll().block();
        agent = createEntity();
    }

    @Test
    void createAgent() throws Exception {
        int databaseSizeBeforeCreate = agentRepository.findAll().collectList().block().size();
        // Create the Agent
        AgentDTO agentDTO = agentMapper.toDto(agent);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(agentDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll().collectList().block();
        assertThat(agentList).hasSize(databaseSizeBeforeCreate + 1);
        Agent testAgent = agentList.get(agentList.size() - 1);
        assertThat(testAgent.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAgent.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testAgent.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testAgent.getRegistrationDate()).isEqualTo(DEFAULT_REGISTRATION_DATE);
    }

    @Test
    void createAgentWithExistingId() throws Exception {
        // Create the Agent with an existing ID
        agent.setId("existing_id");
        AgentDTO agentDTO = agentMapper.toDto(agent);

        int databaseSizeBeforeCreate = agentRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(agentDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll().collectList().block();
        assertThat(agentList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void getAllAgents() {
        // Initialize the database
        agentRepository.save(agent).block();

        // Get all the agentList
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
            .value(hasItem(agent.getId()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION))
            .jsonPath("$.[*].active")
            .value(hasItem(DEFAULT_ACTIVE.booleanValue()))
            .jsonPath("$.[*].registrationDate")
            .value(hasItem(DEFAULT_REGISTRATION_DATE.toString()));
    }

    @Test
    void getAgent() {
        // Initialize the database
        agentRepository.save(agent).block();

        // Get the agent
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, agent.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(agent.getId()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION))
            .jsonPath("$.active")
            .value(is(DEFAULT_ACTIVE.booleanValue()))
            .jsonPath("$.registrationDate")
            .value(is(DEFAULT_REGISTRATION_DATE.toString()));
    }

    @Test
    void getNonExistingAgent() {
        // Get the agent
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingAgent() throws Exception {
        // Initialize the database
        agentRepository.save(agent).block();

        int databaseSizeBeforeUpdate = agentRepository.findAll().collectList().block().size();

        // Update the agent
        Agent updatedAgent = agentRepository.findById(agent.getId()).block();
        updatedAgent.name(UPDATED_NAME).description(UPDATED_DESCRIPTION).active(UPDATED_ACTIVE).registrationDate(UPDATED_REGISTRATION_DATE);
        AgentDTO agentDTO = agentMapper.toDto(updatedAgent);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, agentDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(agentDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll().collectList().block();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
        Agent testAgent = agentList.get(agentList.size() - 1);
        assertThat(testAgent.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAgent.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testAgent.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testAgent.getRegistrationDate()).isEqualTo(UPDATED_REGISTRATION_DATE);
    }

    @Test
    void putNonExistingAgent() throws Exception {
        int databaseSizeBeforeUpdate = agentRepository.findAll().collectList().block().size();
        agent.setId(UUID.randomUUID().toString());

        // Create the Agent
        AgentDTO agentDTO = agentMapper.toDto(agent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, agentDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(agentDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll().collectList().block();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchAgent() throws Exception {
        int databaseSizeBeforeUpdate = agentRepository.findAll().collectList().block().size();
        agent.setId(UUID.randomUUID().toString());

        // Create the Agent
        AgentDTO agentDTO = agentMapper.toDto(agent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(agentDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll().collectList().block();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamAgent() throws Exception {
        int databaseSizeBeforeUpdate = agentRepository.findAll().collectList().block().size();
        agent.setId(UUID.randomUUID().toString());

        // Create the Agent
        AgentDTO agentDTO = agentMapper.toDto(agent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(agentDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll().collectList().block();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateAgentWithPatch() throws Exception {
        // Initialize the database
        agentRepository.save(agent).block();

        int databaseSizeBeforeUpdate = agentRepository.findAll().collectList().block().size();

        // Update the agent using partial update
        Agent partialUpdatedAgent = new Agent();
        partialUpdatedAgent.setId(agent.getId());

        partialUpdatedAgent.name(UPDATED_NAME).registrationDate(UPDATED_REGISTRATION_DATE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAgent.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedAgent))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll().collectList().block();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
        Agent testAgent = agentList.get(agentList.size() - 1);
        assertThat(testAgent.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAgent.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testAgent.getActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testAgent.getRegistrationDate()).isEqualTo(UPDATED_REGISTRATION_DATE);
    }

    @Test
    void fullUpdateAgentWithPatch() throws Exception {
        // Initialize the database
        agentRepository.save(agent).block();

        int databaseSizeBeforeUpdate = agentRepository.findAll().collectList().block().size();

        // Update the agent using partial update
        Agent partialUpdatedAgent = new Agent();
        partialUpdatedAgent.setId(agent.getId());

        partialUpdatedAgent
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .active(UPDATED_ACTIVE)
            .registrationDate(UPDATED_REGISTRATION_DATE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAgent.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedAgent))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll().collectList().block();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
        Agent testAgent = agentList.get(agentList.size() - 1);
        assertThat(testAgent.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAgent.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testAgent.getActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testAgent.getRegistrationDate()).isEqualTo(UPDATED_REGISTRATION_DATE);
    }

    @Test
    void patchNonExistingAgent() throws Exception {
        int databaseSizeBeforeUpdate = agentRepository.findAll().collectList().block().size();
        agent.setId(UUID.randomUUID().toString());

        // Create the Agent
        AgentDTO agentDTO = agentMapper.toDto(agent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, agentDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(agentDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll().collectList().block();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchAgent() throws Exception {
        int databaseSizeBeforeUpdate = agentRepository.findAll().collectList().block().size();
        agent.setId(UUID.randomUUID().toString());

        // Create the Agent
        AgentDTO agentDTO = agentMapper.toDto(agent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(agentDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll().collectList().block();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamAgent() throws Exception {
        int databaseSizeBeforeUpdate = agentRepository.findAll().collectList().block().size();
        agent.setId(UUID.randomUUID().toString());

        // Create the Agent
        AgentDTO agentDTO = agentMapper.toDto(agent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(agentDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Agent in the database
        List<Agent> agentList = agentRepository.findAll().collectList().block();
        assertThat(agentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteAgent() {
        // Initialize the database
        agentRepository.save(agent).block();

        int databaseSizeBeforeDelete = agentRepository.findAll().collectList().block().size();

        // Delete the agent
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, agent.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Agent> agentList = agentRepository.findAll().collectList().block();
        assertThat(agentList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
