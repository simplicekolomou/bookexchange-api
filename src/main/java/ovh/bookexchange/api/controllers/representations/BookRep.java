package ovh.bookexchange.api.controllers.representations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ovh.bookexchange.api.domains.entities.AvailabilityType;
import ovh.bookexchange.api.domains.entities.PhysicalState;
import java.util.List;

@ToString
@Getter
@Setter
public class BookRep {
    private long id;

    @NotNull
    private PhysicalState physicalState;

    @NotNull
    private AvailabilityType availabilityType;

    @PositiveOrZero
    private long askingPrice;

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
