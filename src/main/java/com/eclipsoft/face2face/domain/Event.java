package com.eclipsoft.face2face.domain;

import com.eclipsoft.face2face.domain.enumeration.EventType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A Event.
 */
@Document(collection = "event")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("identification")
    private String identification;

    @Field("dactilar")
    private String dactilar;

    @Field("validation_date")
    private Instant validationDate;

    @Field("successful")
    private Boolean successful;

    @Field("event_type")
    private EventType eventType;

    @Field("agent")
    @JsonIgnoreProperties(value = { "events" }, allowSetters = true)
    private Agent agent;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public Event id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdentification() {
        return this.identification;
    }

    public Event identification(String identification) {
        this.setIdentification(identification);
        return this;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getDactilar() {
        return this.dactilar;
    }

    public Event dactilar(String dactilar) {
        this.setDactilar(dactilar);
        return this;
    }

    public void setDactilar(String dactilar) {
        this.dactilar = dactilar;
    }

    public Instant getValidationDate() {
        return this.validationDate;
    }

    public Event validationDate(Instant validationDate) {
        this.setValidationDate(validationDate);
        return this;
    }

    public void setValidationDate(Instant validationDate) {
        this.validationDate = validationDate;
    }

    public Boolean getSuccessful() {
        return this.successful;
    }

    public Event successful(Boolean successful) {
        this.setSuccessful(successful);
        return this;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public EventType getEventType() {
        return this.eventType;
    }

    public Event eventType(EventType eventType) {
        this.setEventType(eventType);
        return this;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Agent getAgent() {
        return this.agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public Event agent(Agent agent) {
        this.setAgent(agent);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Event)) {
            return false;
        }
        return id != null && id.equals(((Event) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Event{" +
            "id=" + getId() +
            ", identification='" + getIdentification() + "'" +
            ", dactilar='" + getDactilar() + "'" +
            ", validationDate='" + getValidationDate() + "'" +
            ", successful='" + getSuccessful() + "'" +
            ", eventType='" + getEventType() + "'" +
            "}";
    }
}
