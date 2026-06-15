package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ovh.bookexchange.api.domains.entities.notifications.NotifSub;

import java.util.List;

@ToString
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

    private boolean isVisible;

    private String profilePicture;

    @Size(max=100)
    private String bio;

    @Embedded
    private Adress adress;

    @Embedded
    private NotifSub notifSub;

    @OneToMany(mappedBy = "endUser")
    @NotNull
    private List<Membership> memberships;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    @NotNull
    private List<Message> messages;

    @OneToMany(mappedBy = "endUser")
    @NotNull
    private List<Payement> payements;

    @OneToMany(mappedBy = "owner")
    @NotNull
    @ToString.Exclude
    private List<BookCopy> collection;

    @ManyToMany
    @NotNull
    private List<Book> whisedList;

    @ManyToMany
    @NotNull
    private List<EndUser> friends;
    @Column(length = 512)
    private String fcmToken;  // Stocke le token firebase cloud messaging pour ce user

    public EndUser() {}

    public EndUser(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.memberships = List.of();
        this.messages = List.of();
        this.payements = List.of();
        this.collection = List.of();
        this.whisedList = List.of();
        this.friends = List.of();
    }
}
