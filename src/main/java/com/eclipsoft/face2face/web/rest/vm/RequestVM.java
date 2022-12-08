package com.eclipsoft.face2face.web.rest.vm;


import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class RequestVM {

    @NotNull
    @NotEmpty
    @Size(max = 10, min = 10)
    String id;

    @Size(min = 6, max = 6)
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
