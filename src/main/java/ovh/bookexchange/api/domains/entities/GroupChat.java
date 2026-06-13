package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class GroupChat {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    @NotNull
    private GroupType groupType;

    @OneToMany(mappedBy = "groupChat", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    List<Membership> members;

    @OneToMany(mappedBy = "groupChat", cascade = CascadeType.ALL)
    @NotNull
    List<Message> messages;

    public boolean isMember(EndUser user) {
        return members.stream().anyMatch(m -> m.getEndUser().getId() == user.getId());
    }
}
