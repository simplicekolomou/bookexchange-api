package ovh.bookexchange.api.controllers.representations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@ToString
@Getter
@Setter
public class BookWishRep {
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

    @PositiveOrZero
    private long ownerId;
}
