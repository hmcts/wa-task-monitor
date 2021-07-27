package uk.gov.hmcts.reform.wataskmonitor.services;

public interface UpdateCaseDataService {

    boolean updateCaseInCcd(String caseId, String serviceToken);
}
