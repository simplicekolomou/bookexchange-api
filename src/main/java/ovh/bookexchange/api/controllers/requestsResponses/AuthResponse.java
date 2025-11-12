package ovh.bookexchange.api.controllers.requestsResponses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthResponse {
    private String accessToken;
}
