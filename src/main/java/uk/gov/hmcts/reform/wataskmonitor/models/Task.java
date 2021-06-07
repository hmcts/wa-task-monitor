package uk.gov.hmcts.reform.wataskmonitor.models;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Task {
    private String id;

    // to deserialize
    public Task() {
    }

    public Task(String id) {
        this.id = id;
    }

    // to serialize
    public void setId(String id) {
        this.id = id;
    }
}
