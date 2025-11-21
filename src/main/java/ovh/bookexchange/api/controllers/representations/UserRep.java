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
    @Setter(AccessLevel.NONE)
    private long id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Size(max = 255)
    @Column(unique = true)
    private String email;

    private boolean isAdmin;

    private boolean isVisible;

    private String profilePicture;

    private String bio;

    @Embedded
    private Adress adress;

    public UserRep() {
    }
}
