package uk.gov.hmcts.reform.wataskmonitor.clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCaseList;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskmonitor.clients.CaseEventHandlerClient.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "ccd-client",
    url = "${core_case_data.api.search.url}"
)
@SuppressWarnings("PMD.UseObjectForClearerAPI")
public interface CcdClient {

    @PostMapping(value = "/searchCases?ctid={caseType}",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    ElasticSearchCaseList searchCases(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("caseType") String caseType,
        @RequestBody String searchString
    );

}
