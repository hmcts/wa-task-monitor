package uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.CaseIdList;

import java.util.List;

@ToString
@EqualsAndHashCode(callSuper = false)
@Getter
public class JsonResourceCaseList implements CaseIdList {

    List<String> caseIds;

    @JsonCreator
    public JsonResourceCaseList(@JsonProperty("caseIds") List<String> caseIds) {
        super();
        this.caseIds = caseIds;
    }

}
