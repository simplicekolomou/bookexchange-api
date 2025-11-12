package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    @Setter(lombok.AccessLevel.NONE)
    private long id;

    @NotBlank
    private String apiId;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    @NotNull
    private List<BookCopy> copies;

    @ManyToMany
    @NotNull
    private List<Author> authors;
}
