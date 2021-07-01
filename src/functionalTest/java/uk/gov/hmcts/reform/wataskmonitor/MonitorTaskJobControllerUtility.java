package uk.gov.hmcts.reform.wataskmonitor;

import java.util.function.Function;

public final class MonitorTaskJobControllerUtility {

    static Function<String, String> expectedResponse = (name) -> "{\n"
                                                                 + "  \"job_details\" : {\n"
                                                                 + "    \"name\" : \"" + name + "\"\n"
                                                                 + "  }\n"
                                                                 + "}";

    private MonitorTaskJobControllerUtility() {
        // utility class should not have a public or default constructor
    }

}
