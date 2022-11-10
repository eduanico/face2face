package com.eclipsoft.face2face.Exception;

import org.zalando.problem.Status;

public class CheckIdBadRequestException extends EsignRuntimeException {

    private static final long serialVersionUID = 1L;

    private static final Status defaultHttpStatus = Status.OK;

    private static final String defaultErrorCode = ErrorConstants.CHECKID_BAD_REQUEST;

    public CheckIdBadRequestException() {
        super("CheckId received a bad request", defaultErrorCode, defaultHttpStatus);
    }

    public CheckIdBadRequestException(String userMessage) {
        super("CheckId received a bad request", defaultErrorCode, defaultHttpStatus, userMessage);
    }
}

