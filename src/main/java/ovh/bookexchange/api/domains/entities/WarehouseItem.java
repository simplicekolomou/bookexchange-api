package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private ItemStatus itemStatus;

    @NotNull
    private String locationSlot; //peut être une chaîne vide si le livre est arrivé ou parti.

    @ManyToOne
    @JoinColumn(nullable=false, updatable = false)
    private BookCopy bookCopy;
}
