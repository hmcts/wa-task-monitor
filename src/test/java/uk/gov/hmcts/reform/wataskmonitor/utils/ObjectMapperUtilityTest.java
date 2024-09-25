package uk.gov.hmcts.reform.wataskmonitor.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.JsonResourceCaseList;
import uk.gov.hmcts.reform.wataskmonitor.exceptions.ObjectMapperUtilityFailure;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.wataskmonitor.utils.ObjectMapperUtility.stringToObject;

class ObjectMapperUtilityTest {

    @Test
    void stringToObjectTest() {
        String source = """
            {
              "caseIds": [
                "1626272789070361",
                "1626272789070362"
              ]
            }
            """;

        JsonResourceCaseList actual = stringToObject(source, JsonResourceCaseList.class);
        assertThat(actual).isEqualTo(new JsonResourceCaseList(List.of("1626272789070361", "1626272789070362")));
    }

    @Test
    void stringToObjectThrowException() {
        assertThatThrownBy(() ->
            stringToObject("invalid source", JsonResourceCaseList.class))
            .hasMessage("Error deserializing object[class "
                        + "uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.JsonResourceCaseList] "
                        + "from string[invalid source]")
            .isInstanceOf(ObjectMapperUtilityFailure.class);
    }
}
