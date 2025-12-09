package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Entity
@Getter
@Setter
public class BookWish {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private long id;

    @NotBlank
    private String title;

    @NotEmpty
    private List<String> authors;

    private String format;

    private String edition;

    private String isbn;

    private String coverPictureApiUrl;

    private String userUploadPicturePath;

    private String description;

    @ManyToOne
    @JoinColumn(nullable = false)
    private EndUser owner;
}
