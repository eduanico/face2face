package com.eclipsoft.face2face.service.mapper;

import com.eclipsoft.face2face.domain.Agent;
import com.eclipsoft.face2face.service.dto.AgentDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Agent} and its DTO {@link AgentDTO}.
 */
@Mapper(componentModel = "spring")
public interface AgentMapper extends EntityMapper<AgentDTO, Agent> {}
