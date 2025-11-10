package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;

    private String penName;

    @ManyToMany(mappedBy = "authors")
    @NotNull
    private List<Book> books;
}
