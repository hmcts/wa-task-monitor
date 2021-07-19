package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskmonitor.clients.CaseEventHandlerClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.caseeventhandler.EventInformation;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCase;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCaseList;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchRetrieverParameter;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobOutcomeService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTaskJobTest {

    public static final String SOME_SERVICE_TOKEN = "some service token";
    @Mock
    private CaseEventHandlerClient caseEventHandlerClient;
    @Mock
    private JobOutcomeService createTaskJobOutcomeService;
    @Mock
    private ElasticSearchCaseRetrieverService elasticSearchCaseRetrieverService;

    @InjectMocks
    private CreateTaskJob createTaskJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, false",
        "CONFIGURATION, false",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false",
        "AD_HOC_CREATE_TASKS, true"
    })
    void canRun(JobName jobName, boolean expectedResult) {
        assertThat(createTaskJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void run() {
        when(createTaskJobOutcomeService.getJobOutcome(eq(SOME_SERVICE_TOKEN), anyString()))
            .thenReturn(CreateTaskJobOutcome.builder().build());
        when(elasticSearchCaseRetrieverService.getCaseIdList(ElasticSearchRetrieverParameter.builder()
                                                                 .authentication("some user token")
                                                                 .serviceAuthentication(SOME_SERVICE_TOKEN)
                                                                 .build()
        )).thenReturn(new ElasticSearchCaseList(List.of(
            new ElasticSearchCase("1626272789070362"),
            new ElasticSearchCase("1626272789070361")
        )));

        createTaskJob.run(SOME_SERVICE_TOKEN);

        verify(caseEventHandlerClient, times(2)).sendMessage(
            eq(SOME_SERVICE_TOKEN),
            argThat(this::eventInformationMatcher)
        );

        verify(createTaskJobOutcomeService, times(2))
            .getJobOutcome(eq(SOME_SERVICE_TOKEN), anyString());
    }

    private boolean eventInformationMatcher(EventInformation eventInformation) {
        return eventInformation.getEventId().equals("buildCase") && eventInformation.getJurisdictionId().equals("ia")
               && eventInformation.getCaseTypeId().equals("asylum")
               && eventInformation.getNewStateId().equals("caseUnderReview")
               && StringUtils.isNotBlank(eventInformation.getEventInstanceId())
               && eventInformation.getEventTimeStamp() != null
               && StringUtils.isNotBlank(eventInformation.getCaseId())
               && StringUtils.isNotBlank(eventInformation.getUserId());
    }
}
