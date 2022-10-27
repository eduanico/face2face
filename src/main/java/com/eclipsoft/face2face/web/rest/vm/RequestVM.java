package com.eclipsoft.face2face.web.rest.vm;


public class RequestVM {

    String id;

    String dactilar;

    String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDactilar() {
        return dactilar;
    }

    public void setDactilar(String dactilar) {
        this.dactilar = dactilar;
    }
}
