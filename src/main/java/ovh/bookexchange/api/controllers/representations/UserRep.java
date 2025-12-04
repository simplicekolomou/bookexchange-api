package ovh.bookexchange.api.controllers.representations;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import ovh.bookexchange.api.domains.entities.*;

@Getter
@Setter
public class UserRep {
    private long id;

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @Size(max = 100)
    private String email;

    private boolean isAdmin;

    private boolean isVisible;

    private String profilePicture;

    @Size(max=100)
    private String bio;

    @Embedded
    private Adress adress;
}
