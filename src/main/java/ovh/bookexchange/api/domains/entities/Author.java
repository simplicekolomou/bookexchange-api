package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private long id;

    private String firstName;
    private String lastName;
    private String penName;

    @ManyToMany(mappedBy = "authors")
    private List<Book> books;
}
