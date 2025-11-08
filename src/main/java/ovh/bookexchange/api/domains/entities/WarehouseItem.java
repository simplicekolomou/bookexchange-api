package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class WarehouseItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(lombok.AccessLevel.NONE)
    private long id;

    private ItemStatus itemStatus;

    private String locationSlot;

    @ManyToOne
    @JoinColumn(nullable=false, updatable = false)
    private BookCopy bookCopy;
}
