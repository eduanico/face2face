package com.eclipsoft.face2face.Exception;

import java.net.URI;

public final class ErrorConstants {

    public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String PROBLEM_BASE_URL = "https://id4sign.eclipsoft.com/problem";
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
    public static final String ERROR_MSG_PREFIX = "error-msg.";
    public static final String INVALID_PASSWORD = "invalid-password";
    public static final String INVALID_OTP = "invalid-otp";
    public static final String LOGIN_ALREADY_USED = "login-already-used";

    public static final String IDENTIFICATION_ALREADY_USED = "identification-already-used";
    public static final String NO_SUCH_EMAIL = "no-such-email";
    public static final String NO_SUCH_USER = "no-such-user";
    public static final String EMAIL_ALREADY_USED = "email-already-used";
    public static final String EMAIL_ALREADY_VERIFIED = "email-already-verified";
    public static final String INVALID_VERIFICATION_CODE = "invalid-verification-code";
    public static final String PARAMETER_ALREADY_EXISTS = "parameter-already-exists";
    public static final String INVALID_HCAPTCHA_TOKEN = "invalid-hcaptcha-token";
    public static final String CHECKID_INTEGRATION_FAIL = "checkid-integration-fail";
    public static final String CHECKID_BAD_REQUEST = "checkid-bad-request";
    public static final String DECEASED_CITIZEN = "deceased-citizen";
    public static final String MINOR_CITIZEN = "minor-citizen";
    public static final String EXPIRED_IDENTITY_DOCUMENT = "expired-identity-document";
    public static final String CREDENTIAL_LOCKED = "credential-locked";
    public static final String CREDENTIAL_INVALID = "credential-invalid";

    private ErrorConstants() {}
}
