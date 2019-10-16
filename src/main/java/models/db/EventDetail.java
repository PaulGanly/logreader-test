package models.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "event_detail")
public class EventDetail {

    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "type")
    private String type;

    @Column(name = "host")
    private Integer host;

    @Column(name = "alert")
    private Boolean alert;
}
