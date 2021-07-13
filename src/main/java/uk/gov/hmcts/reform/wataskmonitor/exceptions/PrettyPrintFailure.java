package uk.gov.hmcts.reform.wataskmonitor.exceptions;

public class PrettyPrintFailure extends RuntimeException {

    private static final long serialVersionUID = -6206663926409296465L;

    public PrettyPrintFailure(String message, Throwable cause) {
        super(message, cause);
    }
}
