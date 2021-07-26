package uk.gov.hmcts.reform.wataskmonitor.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import uk.gov.hmcts.reform.wataskmonitor.exceptions.CamundaRequestFailure;
import uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class ResourceUtility {

    private ResourceUtility() {
        // utility class should not have a public or default constructor
    }

    public static String getResource(ResourceEnum resource) {
        try (var is = new ClassPathResource(resource.getResourcePath()).getInputStream()) {
            return FileCopyUtils.copyToString(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new CamundaRequestFailure("Error loading file: " + resource, exception);
        }
    }

}
