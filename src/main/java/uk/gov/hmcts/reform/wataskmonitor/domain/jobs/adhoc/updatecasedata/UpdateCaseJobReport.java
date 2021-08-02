package uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.updatecasedata;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.JobReport;

import java.util.List;

@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = false)
@Getter
public class UpdateCaseJobReport extends JobReport {

    @ToString.Include
    private final int totalNumberOfCasesInSearchResult;
    @ToString.Include
    private final List<UpdateCaseJobOutcome> outcomeList;

    public UpdateCaseJobReport(int totalNumberOfCasesInSearchResult, List<UpdateCaseJobOutcome> outcomeList) {
        super();
        this.totalNumberOfCasesInSearchResult = totalNumberOfCasesInSearchResult;
        this.outcomeList = outcomeList;
    }

    @ToString.Include
    public long getTotalNumberOfUpdatedCases() {
        return outcomeList.stream()
            .filter(UpdateCaseJobOutcome::isUpdated)
            .count();
    }

    @ToString.Include
    public long getTotalNumberOfNonUpdatedCases() {
        return outcomeList.stream()
            .filter(outcome -> !outcome.isUpdated())
            .count();

    }

    @ToString.Include
    public long getTotalNumberOfCasesProcessed() {
        return outcomeList.size();
    }

}
