package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
public class Payement {
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private long id;
    private long amount; //en cents (pas d'erreur d'arrondi sur des floats avec de l'argent)
    private boolean isPaid;
    @NotNull
    private Timestamp payementTime;
    @NotBlank
    private String payementMethod;
    @ManyToOne
    private EndUser endUser;
}
