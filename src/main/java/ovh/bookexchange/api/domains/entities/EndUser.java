package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.*;
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
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String profilePicture;
    private String bio;

    @Embedded
    private Adress adress;

    @OneToMany(mappedBy = "endUsers")
    private List<Membership> memberships;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<Message> messages;

    @OneToMany
    private List<BookCopy> collection;

    @OneToMany
    private List<Book> whisedList;




    @ManyToMany
    private List<EndUser> friends;
}
