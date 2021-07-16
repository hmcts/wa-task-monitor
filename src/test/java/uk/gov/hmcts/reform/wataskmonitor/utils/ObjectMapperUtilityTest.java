package uk.gov.hmcts.reform.wataskmonitor.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CaseIdList;
import uk.gov.hmcts.reform.wataskmonitor.exceptions.ObjectMapperUtilityFailure;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.wataskmonitor.utils.ObjectMapperUtility.stringToObject;

class ObjectMapperUtilityTest {

    @Test
    void stringToObjectTest() {
        String source = "{\n"
                        + "  \"caseIds\": [\n"
                        + "    \"1626272789070361\",\n"
                        + "    \"1626272789070362\"\n"
                        + "  ]\n"
                        + "}\n";

        CaseIdList actual = stringToObject(source, CaseIdList.class);
        assertThat(actual).isEqualTo(new CaseIdList(List.of("1626272789070361", "1626272789070362")));
    }

    @Test
    void stringToObjectThrowException() {
        assertThatThrownBy(() ->
                               stringToObject("invalid source", CaseIdList.class))
            .hasMessage("Error deserializing "
                        + "object[class uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CaseIdList] "
                        + "from string[invalid source]")
            .isInstanceOf(ObjectMapperUtilityFailure.class);
    }
}
