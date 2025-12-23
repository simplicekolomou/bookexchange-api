package ovh.bookexchange.api.domains.entities.notifications;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class NotifSub {
    @NotBlank
    private String endpoint;

    @Embedded
    @NotNull
    private NotifSubKeys keys;
}
