package ovh.bookexchange.api.controllers.requestsResponses;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdatePasswordRequest {
    private String currentPassword;
    @NotBlank(message = "Password is required")
    @Size(max=255, message = "Password must be less than 256 characters")
    private String newPassword;
}
