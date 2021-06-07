package uk.gov.hmcts.reform.wataskmonitor.models;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class CamundaTask {

    @SuppressWarnings("PMD.UnusedPrivateField")
    private String id;

    public CamundaTask() {
        // to deserialize
    }

    public CamundaTask(String id) {
        this.id = id;
    }

    public void setId(String id) {
        // to serialize
        this.id = id;
    }
}
