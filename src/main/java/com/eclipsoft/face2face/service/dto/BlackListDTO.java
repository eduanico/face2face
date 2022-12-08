package com.eclipsoft.face2face.service.dto;

import java.io.Serializable;

@SuppressWarnings("common-java:DuplicatedBlocks")
public class BlackListDTO implements Serializable {

    private String identificacion;

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }
}
