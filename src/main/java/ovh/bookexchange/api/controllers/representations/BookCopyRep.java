package ovh.bookexchange.api.controllers.representations;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ovh.bookexchange.api.domains.entities.*;

@Getter
@Setter
public class BookCopyRep {
    private long id;

    @NotNull
    private PhysicalState physicalState;

    @NotNull
    private AvailabilityType availabilityType;

    @NotNull
    private Book book;

    private long ownerId;
}
