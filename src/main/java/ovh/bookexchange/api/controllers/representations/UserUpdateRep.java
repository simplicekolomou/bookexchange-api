package ovh.bookexchange.api.controllers.representations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRep {
    @NotBlank(message = "First name is required")
    @Size(max=255, message = "First name must be less than 256 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max=255, message = "Last name must be less than 256 characters")
    private String lastName;

    @Size(max=100, message = "Localite must be less than 256 characters")
    private String localite;

    @Size(max=100, message = "Bio must be less than 2048 characters")
    private String bio;

    private boolean isVisible;
}
