package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Membership {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private long id;

    private boolean notification;

    @ManyToOne
    @JoinColumn(nullable = false)
    private EndUser endUsers;

    @ManyToOne
    @JoinColumn(nullable = false)
    private GroupChat groupChat;
}
