package ovh.bookexchange.api.controllers.requestsResponses;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ForgotPasswordRequest {
    @NotBlank(message = "Email is required")
    @Size(max=255, message = "Email must be less than 256 characters")
    private String email;
}
