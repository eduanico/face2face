package com.eclipsoft.face2face.service.mapper;

import com.eclipsoft.face2face.domain.BlackList;
import com.eclipsoft.face2face.service.dto.BlackListDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity {@link BlackList} and its DTO {@link BlackListDTO}.
 */
@Mapper(componentModel = "spring")
public interface BlackListMapper extends EntityMapper<BlackListDTO, BlackList> {
}
