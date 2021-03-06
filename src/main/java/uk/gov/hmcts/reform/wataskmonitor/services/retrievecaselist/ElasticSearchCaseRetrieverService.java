package uk.gov.hmcts.reform.wataskmonitor.services.retrievecaselist;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CcdClient;
import uk.gov.hmcts.reform.wataskmonitor.config.idam.IdamTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.ElasticSearchRetrieverParameter;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCaseList;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

@Component
@Slf4j
public class ElasticSearchCaseRetrieverService implements RetrieveCaseListService<ElasticSearchRetrieverParameter> {

    private final CcdClient ccdClient;
    private final IdamTokenGenerator systemUserIdamToken;

    public ElasticSearchCaseRetrieverService(CcdClient ccdClient, IdamTokenGenerator systemUserIdamToken) {
        this.ccdClient = ccdClient;
        this.systemUserIdamToken = systemUserIdamToken;
    }

    @Override
    public ElasticSearchCaseList retrieveCaseList(ElasticSearchRetrieverParameter param) {
        log.info("Retrieving case list from CCD...");
        if (StringUtils.isBlank(param.getServiceAuthentication())) {
            throw new IllegalArgumentException("service token is missing");
        }
        return ccdClient.searchCases(
            systemUserIdamToken.generate(),
            param.getServiceAuthentication(),
            "Asylum",
            ResourceUtility.getResource(param.getResourceEnum())
        );
    }
}
