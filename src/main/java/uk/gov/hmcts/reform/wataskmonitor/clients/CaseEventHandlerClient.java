package uk.gov.hmcts.reform.wataskmonitor.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.wataskmonitor.config.SnakeCaseFeignConfiguration;
import uk.gov.hmcts.reform.wataskmonitor.domain.caseeventhandler.EventInformation;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
    name = "caseEventHandler",
    url = "${case-event-handler.service.url}",
    configuration = SnakeCaseFeignConfiguration.class
)
public interface CaseEventHandlerClient {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @PostMapping(value = "/messages",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    String sendMessage(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                       @Valid @RequestBody EventInformation eventInformation);

}
