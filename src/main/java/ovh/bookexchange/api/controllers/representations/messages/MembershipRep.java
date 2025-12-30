package ovh.bookexchange.api.controllers.representations.messages;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipRep {
    private long id;
    private boolean notification;
    @Positive
    @NotNull
    private long endUserId;
    private long groupChatId;
}
