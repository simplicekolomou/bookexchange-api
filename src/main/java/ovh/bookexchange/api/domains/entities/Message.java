package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank
    private String content;

    @NotNull
    private Timestamp sendTime;

    @ManyToOne
    @NotNull
    private GroupChat groupChat;

    @ManyToOne
    @NotNull
    private EndUser sender;

    @ManyToMany
    @NotNull
    private List<EndUser> read;
}
