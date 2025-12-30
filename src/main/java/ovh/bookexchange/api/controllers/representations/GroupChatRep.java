package ovh.bookexchange.api.controllers.representations;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ovh.bookexchange.api.domains.entities.Message;

import java.util.List;

@Setter
@Getter
public class GroupChatRep {
    private long id;

    @Valid
    @Size(min = 2, max = 20)
    private List<MembershipRep> members;
}
