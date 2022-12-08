package com.eclipsoft.face2face.config.properties;

import lombok.ToString;

@ToString
public class Integration {

    private CheckId checkId;

    public CheckId getCheckId() {
        return checkId;
    }

    public void setCheckId(CheckId checkId) {
        this.checkId = checkId;
    }

}
