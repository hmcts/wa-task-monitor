package uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CamundaVariableDefinitionTest {

    @Test
    void should_return_correct_value_from_CamundaVariableDefinition_value_and_from_methods() {

        HashMap<String, Object> mappedValues = new HashMap<>();
        mappedValues.put(CamundaVariableDefinition.CASE_ID.value(), "otherCaseId");
        assertEquals("otherCaseId", mappedValues.get(CamundaVariableDefinition.CASE_ID.value()));
        assertEquals(Optional.of(CamundaVariableDefinition.CASE_ID), CamundaVariableDefinition.from("caseId"));

    }

}