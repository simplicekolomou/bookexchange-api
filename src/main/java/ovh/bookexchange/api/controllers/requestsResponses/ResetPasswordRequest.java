package ovh.bookexchange.api.controllers.requestsResponses;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    private String token;
    @NotBlank(message = "Password is required")
    @Size(max=255, message = "Password must be less than 256 characters")
    private String password;
}
