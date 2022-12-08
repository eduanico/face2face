package com.eclipsoft.face2face.Exception;

import org.zalando.problem.Status;

public class EsignRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public static final Status defaultHttpStatus = Status.CONFLICT;

    private final String errorCode;

    private final Status httpStatus;

    private String userMessage;

    public EsignRuntimeException(String technicalMessage, String errorCode) {
        super(technicalMessage);
        this.errorCode = errorCode;
        this.httpStatus = defaultHttpStatus;
    }

    public EsignRuntimeException(String technicalMessage, String errorCode, Status httpStatus) {
        super(technicalMessage);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public EsignRuntimeException(String technicalMessage, String errorCode, Status httpStatus, String userMessage) {
        super(technicalMessage);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.userMessage = userMessage;
    }

    public EsignRuntimeException(String technicalMessage, String errorCode, Throwable cause) {
        super(technicalMessage, cause);
        this.errorCode = errorCode;
        this.httpStatus = defaultHttpStatus;
    }

    protected EsignRuntimeException(String technicalMessage, String errorCode, Throwable cause, Status httpStatus) {
        super(technicalMessage, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public Status getHttpStatus() { return this.httpStatus; }

    public String getUserMessage() {
        return this.userMessage;
    }
}

