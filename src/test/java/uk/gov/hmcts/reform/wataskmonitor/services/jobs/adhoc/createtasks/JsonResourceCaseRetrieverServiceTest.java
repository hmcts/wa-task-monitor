package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.RetrieveCaseListParam;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.JsonResourceCaseList;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonResourceCaseRetrieverServiceTest {

    @Test
    void retrieveCaseList() {
        JsonResourceCaseRetrieverService jsonResourceCaseRetrieverService = new JsonResourceCaseRetrieverService();
        JsonResourceCaseList actual = jsonResourceCaseRetrieverService.retrieveCaseList(
            new RetrieveCaseListParam());

        assertThat(actual).isEqualTo(new JsonResourceCaseList(List.of(
            "1626272789070361",
            "1626272789070362",
            "1626272789070363",
            "1626272789070364"
        )));

    }
}
