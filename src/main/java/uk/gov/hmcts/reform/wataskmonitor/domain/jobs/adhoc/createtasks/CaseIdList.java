package uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
@Getter
public class CaseIdList {
    List<String> caseIds;

    @JsonCreator
    public CaseIdList(@JsonProperty("caseIds") List<String> caseIds) {
        this.caseIds = caseIds;
    }

}
