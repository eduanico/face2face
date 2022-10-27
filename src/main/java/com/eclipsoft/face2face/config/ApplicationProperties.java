package com.eclipsoft.face2face.config;

import com.eclipsoft.face2face.config.properties.Integration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Esigngateway.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private String supportEmail = "desarrollo2@eclipsoft.com";

    private final Integration integration = new Integration();

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public Integration getIntegration() {
        return integration;
    }

}
