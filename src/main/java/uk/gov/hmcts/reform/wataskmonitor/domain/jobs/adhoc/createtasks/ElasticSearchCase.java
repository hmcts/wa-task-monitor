package uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
public class ElasticSearchCase {

    private final String id;

    @JsonCreator
    public ElasticSearchCase(@JsonProperty("id") String id) {
        this.id = id;
    }
}
