package uk.gov.hmcts.reform.wataskmonitor.domain.jobs.caseeventhandler;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.JobReport;

import java.util.List;

@ToString
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Getter
public class MessageJobReport extends JobReport {
    private final int messageCount;
    private final List<String> messageIds;
}
