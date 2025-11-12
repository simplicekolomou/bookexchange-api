package ovh.bookexchange.api.controllers.requestsResponses;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Size(max=255, message = "Email must be less than 256 characters")
    private String email;
    @NotBlank(message = "Password is required")
    @Size(max=255, message = "Password must be less than 256 characters")
    private String password;
    @NotBlank(message = "First name is required")
    @Size(max=255, message = "First name must be less than 256 characters")
    private String firstName;
    @NotBlank(message = "Last name is required")
    @Size(min=1, max=255, message = "Last name must be less than 256 characters")
    private String lastName;
}
