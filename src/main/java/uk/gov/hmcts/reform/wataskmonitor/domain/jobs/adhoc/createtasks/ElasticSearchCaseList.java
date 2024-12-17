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
public class ElasticSearchCaseList implements CaseIdList {

    private final int total;
    private final List<ElasticSearchCase> cases;

    @JsonCreator
    public ElasticSearchCaseList(@JsonProperty("total") int total,
                                 @JsonProperty("cases") List<ElasticSearchCase> cases) {
        super();
        this.total = total;
        this.cases = cases;
    }

}
