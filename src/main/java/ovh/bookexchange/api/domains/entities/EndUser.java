package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class EndUser {
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

    @NotBlank
    @Size(max = 255)
    private String password;

    private boolean isAdmin;

    private String profilePicture;

    private String bio;

    @Embedded
    @NotNull
    private Adress adress;

    @OneToMany(mappedBy = "endUsers")
    @NotNull
    private List<Membership> memberships;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    @NotNull
    private List<Message> messages;

    @OneToMany(mappedBy = "endUser")
    @NotNull
    private List<Payement> payements;

    @OneToMany
    @NotNull
    private List<BookCopy> collection;

    @ManyToMany
    @NotNull
    private List<Book> whisedList;

    @ManyToMany
    @NotNull
    private List<EndUser> friends;
}
