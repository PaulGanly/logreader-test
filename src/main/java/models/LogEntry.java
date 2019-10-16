package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

@Data
@ToString
public class LogEntry implements Comparable<LogEntry> {

    private String id;
    private LogEntryState state;
    private String type;
    private Integer host;
    private Long timestamp;

    @JsonCreator
    public LogEntry(@JsonProperty("id") final String id,
                    @JsonProperty("state") final String state,
                    @JsonProperty("type") final String type,
                    @JsonProperty("host") final String host,
                    @JsonProperty("timestamp") final String timestamp) {
        this.id = id;
        this.state = LogEntryState.getByName(state);
        this.type = type;
        this.host = StringUtils.isNumeric(host) ? Integer.parseInt(host) : null;
        this.timestamp = StringUtils.isNumeric(timestamp) ? Long.parseLong(timestamp) : null;
    }

    @Override
    public int compareTo(LogEntry logEntry){
        return Comparator.comparing(LogEntry::getId)
                .thenComparing(entry -> {
                    if(LogEntryState.STARTED.equals(entry.getState())){
                        return -1;
                    } else {
                        return 1;
                    }
                })
                .compare(this, logEntry);
    }
}
