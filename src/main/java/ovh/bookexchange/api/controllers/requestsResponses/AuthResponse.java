package ovh.bookexchange.api.controllers.requestsResponses;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ovh.bookexchange.api.controllers.representations.UserRep;

@Setter
@Getter
public class AuthResponse {
    private String accessToken;
    private UserRep user;
}
