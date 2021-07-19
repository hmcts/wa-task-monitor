package uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.RetrieveCaseIdListParam;

@ToString
@EqualsAndHashCode(callSuper = false)
@Getter
@Builder
public class ElasticSearchRetrieverParameter extends RetrieveCaseIdListParam {

    private final String authentication;
    private final String serviceAuthentication;

}
