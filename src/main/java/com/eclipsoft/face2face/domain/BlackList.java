package com.eclipsoft.face2face.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Document(collection = "blacklist")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BlackList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("identificacion")
    private String identificacion;

    public BlackList id(String id) {
        this.setId(id);
        return this;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
