package com.eclipsoft.face2face.service.mapper;

import com.eclipsoft.face2face.domain.Agent;
import com.eclipsoft.face2face.domain.Event;
import com.eclipsoft.face2face.service.dto.AgentDTO;
import com.eclipsoft.face2face.service.dto.EventDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Event} and its DTO {@link EventDTO}.
 */
@Mapper(componentModel = "spring")
public interface EventMapper extends EntityMapper<EventDTO, Event> {
    @Mapping(target = "agent", source = "agent", qualifiedByName = "agentId")
    EventDTO toDto(Event s);

    @Named("agentId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    AgentDTO toDtoAgentId(Agent agent);
}
