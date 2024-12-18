package uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.RetrieveCaseListParam;
import uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum;

@ToString
@EqualsAndHashCode(callSuper = false)
@Getter
public class ElasticSearchRetrieverParameter implements RetrieveCaseListParam {

    private final String serviceAuthentication;
    private final ResourceEnum resourceEnum;

    public ElasticSearchRetrieverParameter(String serviceAuthentication, ResourceEnum resourceEnum) {
        super();
        this.serviceAuthentication = serviceAuthentication;
        this.resourceEnum = resourceEnum;
    }
}
