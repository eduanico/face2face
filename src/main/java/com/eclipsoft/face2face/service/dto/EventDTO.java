package com.eclipsoft.face2face.service.dto;

import com.eclipsoft.face2face.domain.enumeration.EventType;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.eclipsoft.face2face.domain.Event} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventDTO implements Serializable {

    private String id;

    private String identification;

    private String dactilar;

    private Instant validationDate;

    private Boolean successful;

    private EventType eventType;

    private AgentDTO agent;

    private String detail;


    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getDactilar() {
        return dactilar;
    }

    public void setDactilar(String dactilar) {
        this.dactilar = dactilar;
    }

    public Instant getValidationDate() {
        return validationDate;
    }

    public void setValidationDate(Instant validationDate) {
        this.validationDate = validationDate;
    }

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public AgentDTO getAgent() {
        return agent;
    }

    public void setAgent(AgentDTO agent) {
        this.agent = agent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventDTO)) {
            return false;
        }

        EventDTO eventDTO = (EventDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, eventDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EventDTO{" +
            "id='" + getId() + "'" +
            ", identification='" + getIdentification() + "'" +
            ", dactilar='" + getDactilar() + "'" +
            ", validationDate='" + getValidationDate() + "'" +
            ", successful='" + getSuccessful() + "'" +
            ", eventType='" + getEventType() + "'" +
            ", agent=" + getAgent() +
            "}";
    }
}
