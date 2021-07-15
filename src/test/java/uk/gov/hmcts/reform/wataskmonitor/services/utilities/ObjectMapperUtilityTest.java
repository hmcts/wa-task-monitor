package uk.gov.hmcts.reform.wataskmonitor.services.utilities;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskmonitor.exceptions.ObjectMapperUtilityFailure;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.adhoc.createtasks.CaseIdList;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjectMapperUtilityTest {

    @Test
    void stringToObject() {
        String source = "{\n"
                        + "  \"caseIds\": [\n"
                        + "    \"1626272789070361\",\n"
                        + "    \"1626272789070362\"\n"
                        + "  ]\n"
                        + "}\n";

        CaseIdList actual = ObjectMapperUtility.stringToObject(source, CaseIdList.class);
        assertThat(actual).isEqualTo(new CaseIdList(List.of("1626272789070361", "1626272789070362")));
    }

    @Test
    void stringToObjectThrowException() {
        assertThatThrownBy(() ->
            ObjectMapperUtility.stringToObject("invalid source", CaseIdList.class))
            .hasMessage("Error deserializing "
                        + "object[class uk.gov.hmcts.reform.wataskmonitor.models.jobs.adhoc.createtasks.CaseIdList] "
                        + "from string[invalid source]")
            .isInstanceOf(ObjectMapperUtilityFailure.class);
    }
}