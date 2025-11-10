package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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

    @ManyToOne
    @JoinColumn(nullable=false)
    private Book book;

    @OneToMany(mappedBy = "bookCopy", cascade = CascadeType.ALL)
    @NotNull
    private List<WarehouseItem> warehouseItems;
}
