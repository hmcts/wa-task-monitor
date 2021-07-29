package uk.gov.hmcts.reform.wataskmonitor.controllers;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles({"integration"})
@SuppressWarnings({"PMD.AbstractClassWithoutAnyMethod", "PMD.AbstractClassWithoutAbstractMethod"})
public class SpringBootIntegrationBaseTest {

    @Autowired
    protected MockMvc mockMvc;

}
