package com.eclipsoft.face2face.Exception;

import org.zalando.problem.Status;

public class CheckIdIntegrationException extends EsignRuntimeException {

    private static final long serialVersionUID = 1L;

    private static final Status defaultHttpStatus = Status.OK;

    private static final String defaultErrorCode = ErrorConstants.CHECKID_INTEGRATION_FAIL;

    public CheckIdIntegrationException(String technicalMessage) {
        super(technicalMessage, defaultErrorCode, defaultHttpStatus);
    }

    public CheckIdIntegrationException(String technicalMessage, Throwable cause) {
        super(technicalMessage, defaultErrorCode, cause, defaultHttpStatus);
    }

}

