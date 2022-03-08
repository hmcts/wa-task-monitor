package uk.gov.hmcts.reform.wataskmonitor.config.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Migration {

    private boolean migrationFlag;
    private String camundaMaxResults;
    private boolean camundaTimeLimitFlag;
    private Long camundaTimeLimit;

}
