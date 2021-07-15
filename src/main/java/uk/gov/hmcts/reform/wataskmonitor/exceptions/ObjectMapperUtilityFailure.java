package uk.gov.hmcts.reform.wataskmonitor.exceptions;

public class ObjectMapperUtilityFailure extends RuntimeException {

    private static final long serialVersionUID = -5109151126523392354L;

    public ObjectMapperUtilityFailure(String message, Throwable cause) {
        super(message, cause);
    }
}
