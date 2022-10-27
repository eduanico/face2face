package com.eclipsoft.face2face.service.mapper;

import com.eclipsoft.face2face.service.dto.PersonDTO;
import com.eclipsoft.face2face.service.utils.Utils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PersonMapper {

    public PersonDTO toDto(Map<String, Object> map) {
        PersonDTO personDTO = new PersonDTO();
        personDTO.setNombreCompleto(map.get("nombre").toString());
        personDTO.setNombres(map.get("nombres").toString());
        personDTO.setApellidos(map.get("apellidos").toString());
        personDTO.setFotografia(map.get("fotografia").toString());

        if(map.containsKey("fechaNacimiento") && map.get("fechaNacimiento") != null) {
            personDTO.setFechaNacimiento(Utils.stringToLocalDate(map.get("fechaNacimiento").toString(), "yyyy-MM-d"));
        }

        if(map.containsKey("fechaCedulacion") && map.get("fechaCedulacion") != null) {
            personDTO.setFechaCedulacion(Utils.stringToLocalDate(map.get("fechaCedulacion").toString(), "yyyy-MM-d"));
        }

        if(map.containsKey("fechaFallecimiento") && map.get("fechaFallecimiento") != null) {
            personDTO.setFechaFallecimiento(Utils.stringToLocalDate(map.get("fechaFallecimiento").toString(), "yyyy-MM-d"));
        }

        return personDTO;
    }

}
