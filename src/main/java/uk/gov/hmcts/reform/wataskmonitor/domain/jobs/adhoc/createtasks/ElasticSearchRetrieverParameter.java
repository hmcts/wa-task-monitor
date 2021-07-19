package uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.RetrieveCaseListParam;

@ToString
@EqualsAndHashCode(callSuper = false)
@Getter
@Builder
public class ElasticSearchRetrieverParameter extends RetrieveCaseListParam {

    private final String serviceAuthentication;

}
