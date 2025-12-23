package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Entity
@Getter
@Setter
public class BookCopy {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private long id;

    @NotNull
    private PhysicalState physicalState;

    @NotNull
    private AvailabilityType availabilityType;

    @PositiveOrZero
    private long askingPrice; //en cents

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

    @OneToMany(mappedBy = "bookCopy", cascade = CascadeType.ALL)
    @NotNull
    private List<WarehouseItem> warehouseItems;
}