package uk.gov.hmcts.reform.wataskmonitor.controllers;

import java.util.function.Function;

public final class MonitorTaskJobControllerUtility {

    public static final Function<String, String> expectedResponse =
        name -> "{\"job_details\":{\"name\":\"" + name + "\"}}";

    private MonitorTaskJobControllerUtility() {
        // utility class should not have a public or default constructor
    }

}
