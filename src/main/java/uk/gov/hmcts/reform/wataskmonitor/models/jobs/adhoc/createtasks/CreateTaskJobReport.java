package uk.gov.hmcts.reform.wataskmonitor.models.jobs.adhoc.createtasks;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobReport;

import java.util.List;

@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = false)
@Getter
public class CreateTaskJobReport extends JobReport {

    @ToString.Include
    private final List<CreateTaskJobOutcome> outcomeList;

    public CreateTaskJobReport(List<CreateTaskJobOutcome> outcomeList) {
        super();
        this.outcomeList = outcomeList;
    }

    @ToString.Include
    public long getTotalNumberOfCreatedTasks() {
        return outcomeList.stream()
            .filter(CreateTaskJobOutcome::isCreated)
            .count();
    }

    @ToString.Include
    public long getTotalNumberOfNonCreatedTasks() {
        return outcomeList.stream()
            .filter(outcome -> !outcome.isCreated())
            .count();

    }

    @ToString.Include
    public long getTotalNumberOfCasesProcessed() {
        return outcomeList.size();
    }

}
