package uk.gov.hmcts.reform.wataskmonitor.domain.jobs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = false)
@Getter
public class GenericJobReport extends JobReport {

    @ToString.Include
    private final int totalTasks;
    @ToString.Include
    private final List<GenericJobOutcome> outcomeList;

    public GenericJobReport(int totalTasks, List<GenericJobOutcome> outcomeList) {
        super();
        this.totalTasks = totalTasks;
        this.outcomeList = outcomeList;
    }

    @ToString.Include
    public long getTotalNumberOfSuccesses() {
        return outcomeList.stream()
            .filter(GenericJobOutcome::isCreated)
            .count();
    }

    @ToString.Include
    public long getTotalNumberOfFailures() {
        return outcomeList.stream()
            .filter(outcome -> !outcome.isCreated())
            .count();
    }

    @ToString.Include
    public long getTotalNumberOfTasksProcessed() {
        return outcomeList.size();
    }

}
