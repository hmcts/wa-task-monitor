package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskmonitor.clients.CaseEventHandlerClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.caseeventhandler.EventInformation;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.ElasticSearchRetrieverParameter;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCase;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCaseList;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobOutcomeService;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.ResourceEnum;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.retrievecaselist.ElasticSearchCaseRetrieverService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTaskJobServiceTest {

    public static final String SOME_SERVICE_TOKEN = "some service token";
    public static final String SOME_CASE_ID_1 = "some case id1";
    public static final String SOME_CASE_ID_2 = "some case id2";
    public static final String SOME_TASK_ID_1 = "some task id1";
    private static final String SOME_TASK_ID_2 = "some task id2";
    public static final String SOME_PROCESS_INSTANCE_ID_1 = "some process instance id1";
    private static final String SOME_PROCESS_INSTANCE_ID_2 = "some process instance id2";
    @Mock
    private CaseEventHandlerClient caseEventHandlerClient;
    @Mock
    private JobOutcomeService createTaskJobOutcomeService;
    @Mock
    private ElasticSearchCaseRetrieverService elasticSearchCaseRetrieverService;

    @InjectMocks
    private CreateTaskJobService createTaskJobService;

    @Test
    void givenCaseEventHandlerThrowsExceptionShouldHandleIt() {
        when(elasticSearchCaseRetrieverService.retrieveCaseList(new ElasticSearchRetrieverParameter(
            SOME_SERVICE_TOKEN,
            ResourceEnum.AD_HOC_CREATE_TASKS_CCD_ELASTIC_SEARCH_QUERY
        ))).thenReturn(new ElasticSearchCaseList(2, List.of(
            new ElasticSearchCase(SOME_CASE_ID_1),
            new ElasticSearchCase(SOME_CASE_ID_2)
        )));

        when(caseEventHandlerClient.sendMessage(eq(SOME_SERVICE_TOKEN), any(EventInformation.class)))
            .thenThrow(new RuntimeException("some exception"));

        CreateTaskJobReport actual = createTaskJobService.createTasks(SOME_SERVICE_TOKEN);

        CreateTaskJobReport expected = new CreateTaskJobReport(
            2,
            List.of(
                CreateTaskJobOutcome.builder()
                    .caseId(SOME_CASE_ID_1)
                    .created(false)
                    .build(),
                CreateTaskJobOutcome.builder()
                    .caseId(SOME_CASE_ID_2)
                    .created(false)
                    .build()
            )
        );
        assertThat(actual).isEqualTo(expected);

        verify(elasticSearchCaseRetrieverService).retrieveCaseList(any(ElasticSearchRetrieverParameter.class));

        verify(caseEventHandlerClient, times(2))
            .sendMessage(eq(SOME_SERVICE_TOKEN), any(EventInformation.class));
        verifyNoInteractions(createTaskJobOutcomeService);
    }

    @Test
    void createTasksHappyPath() {
        CreateTaskJobOutcome taskJobOutcome1 = CreateTaskJobOutcome.builder()
            .caseId(SOME_CASE_ID_1)
            .taskId(SOME_TASK_ID_1)
            .processInstanceId(SOME_PROCESS_INSTANCE_ID_1)
            .created(true)
            .build();
        when(createTaskJobOutcomeService.getJobOutcome(eq(SOME_SERVICE_TOKEN), eq(SOME_CASE_ID_1)))
            .thenReturn(taskJobOutcome1);

        CreateTaskJobOutcome taskJobOutcome2 = CreateTaskJobOutcome.builder()
            .caseId(SOME_CASE_ID_2)
            .taskId(SOME_TASK_ID_2)
            .processInstanceId(SOME_PROCESS_INSTANCE_ID_2)
            .created(true)
            .build();
        when(createTaskJobOutcomeService.getJobOutcome(eq(SOME_SERVICE_TOKEN), eq(SOME_CASE_ID_2)))
            .thenReturn(taskJobOutcome2);

        when(elasticSearchCaseRetrieverService.retrieveCaseList(new ElasticSearchRetrieverParameter(
            SOME_SERVICE_TOKEN,
            ResourceEnum.AD_HOC_CREATE_TASKS_CCD_ELASTIC_SEARCH_QUERY
        ))).thenReturn(new ElasticSearchCaseList(2, List.of(
            new ElasticSearchCase(SOME_CASE_ID_1),
            new ElasticSearchCase(SOME_CASE_ID_2)
        )));

        CreateTaskJobReport actual = createTaskJobService.createTasks(SOME_SERVICE_TOKEN);

        CreateTaskJobReport expected = new CreateTaskJobReport(
            2,
            List.of(taskJobOutcome1, taskJobOutcome2)
        );
        assertThat(actual).isEqualTo(expected);

        verify(elasticSearchCaseRetrieverService).retrieveCaseList(any(ElasticSearchRetrieverParameter.class));

        verify(caseEventHandlerClient, times(2)).sendMessage(
            eq(SOME_SERVICE_TOKEN),
            argThat(this::eventInformationMatcher)
        );

        verify(createTaskJobOutcomeService, times(2))
            .getJobOutcome(eq(SOME_SERVICE_TOKEN), anyString());
    }

    private boolean eventInformationMatcher(EventInformation eventInformation) {
        return eventInformation.getEventId().equals("buildCase")
               && eventInformation.getJurisdictionId().equals("ia")
               && eventInformation.getCaseTypeId().equals("asylum")
               && eventInformation.getNewStateId().equals("caseUnderReview")
               && StringUtils.isNotBlank(eventInformation.getEventInstanceId())
               && eventInformation.getEventTimeStamp() != null
               && StringUtils.isNotBlank(eventInformation.getCaseId())
               && StringUtils.isNotBlank(eventInformation.getUserId());
    }
}
