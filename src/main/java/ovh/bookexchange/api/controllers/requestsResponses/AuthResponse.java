package ovh.bookexchange.api.controllers.requestsResponses;

import lombok.Getter;
import lombok.Setter;
import ovh.bookexchange.api.controllers.representations.UserRep;

@Setter
@Getter
public class AuthResponse {
    private String accessToken;
    private UserRep user;
}
