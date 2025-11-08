package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@Entity
public class Message {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String content;
    private Timestamp sendTime;
    @ManyToOne
    private GroupChat groupChat;
    @ManyToOne
    private EndUser sender;
    @ManyToMany
    private List<EndUser> read;
}
