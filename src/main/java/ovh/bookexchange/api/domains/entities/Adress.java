package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Adress {
    private String postalBoxNumber;
    private String street;
    private String locality;
    private String country;
    private String zipCode;
}
