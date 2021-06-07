package uk.gov.hmcts.reform.wataskmonitor.exceptions;

public class CamundaRequestFailure extends RuntimeException {
    private static final long serialVersionUID = -8074152558915710161L;

    public CamundaRequestFailure(String message, Throwable cause) {
        super(message, cause);
    }
}
