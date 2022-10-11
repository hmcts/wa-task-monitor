package uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TaskFilterOperator;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ExecuteReconfigureTaskFilter.class, name = "ExecuteReconfigureTaskFilter") }
)
public interface TaskFilter<T> {

    String getKey();

    TaskFilterOperator getOperator();

    T getValues();
}
