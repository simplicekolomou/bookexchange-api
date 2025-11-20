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
    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    private String profilePicture;

    private String bio;

    @Embedded
    private Adress adress;

    public UserRep(String firstName, String lastName, String email, boolean isAdmin, String profilePicture, String bio, Adress adress) {
        this(0, firstName, lastName, email, isAdmin, profilePicture, bio, adress);
    }

    public UserRep(long id, String firstName, String lastName, String email, boolean isAdmin, String profilePicture, String bio, Adress adress) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.isAdmin = isAdmin;
        this.profilePicture = profilePicture;
        this.bio = bio;
        this.adress = adress;
    }
}
