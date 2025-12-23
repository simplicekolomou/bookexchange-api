package ovh.bookexchange.api.domains.entities.notifications;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class NotifSubKeys {
    @NotNull
    private String auth;
    @NotNull
    private String p256dh;
}
