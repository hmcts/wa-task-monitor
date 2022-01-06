package uk.gov.hmcts.reform.wataskmonitor;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.AbstractClassWithoutAnyMethod", "PMD.AbstractClassWithoutAbstractMethod"})
public abstract class UnitBaseTest {

    public static final String SOME_SERVICE_TOKEN = "some service token";
    public static final String SOME_USER_TOKEN = "some user token";
    public static final String SOME_USER_ID = "some user id";

    public static final String SOME_CASE_ID = "some case id";
    public static final String SOME_CASE_ID_CAMEL_CASE = "someCaseId";
    public static final String SOME_OTHER_CASE_ID = "some other case id";
    public static final String SOME_CASE_ID_1 = "some case id1";
    public static final String SOME_CASE_ID_2 = "some case id2";
    public static final String SOME_CASE_ID_3 = "some case id3";

    public static final String SOME_TASK_ID_1 = "some task id1";
    public static final String SOME_TASK_ID_2 = "some task id2";
    public static final String SOME_TASK_ID_3 = "some task id3";

    public static final String SOME_PROCESS_INSTANCE_ID_1 = "some process instance id1";
    public static final String SOME_PROCESS_INSTANCE_ID_2 = "some process instance id2";
    public static final String SOME_PROCESS_INSTANCE_ID_3 = "some process instance id3";

    public static final String SOME_ACCESS_TOKEN = "some access token";

}
