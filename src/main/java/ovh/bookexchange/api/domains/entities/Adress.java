package ovh.bookexchange.api.domains.entities;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Adress {
    @Size(max = 10)
    private String postalBoxNumber;
    @Size(max = 100)
    private String street;
    @Size(max = 100)
    private String locality;
    @Size(max = 100)
    private String country;
    @Size(max = 10)
    private String zipCode;
}
