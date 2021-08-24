package uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums;

import java.util.Optional;

import static java.util.Arrays.stream;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public enum CFTTaskState {

    UNCONFIGURED("unconfigured"),
    PENDING_AUTO_ASSIGN("pendingAutoAssign"),
    ASSIGNED("assigned"),
    UNASSIGNED("unassigned"),
    COMPLETED("completed"),
    CANCELLED("cancelled"),
    TERMINATED("terminated"),
    PENDING_RECONFIGURATION("pendingReconfiguration");

    private String value;

    CFTTaskState(String value) {
        this.value = value;
    }

    public static Optional<CFTTaskState> from(
        String value
    ) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    public String getValue() {
        return value;
    }
}
