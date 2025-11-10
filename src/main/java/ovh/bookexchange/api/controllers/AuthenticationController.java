package ovh.bookexchange.api.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ovh.bookexchange.api.controllers.representations.AuthRequest;
import ovh.bookexchange.api.controllers.representations.AuthResponse;

@RestController
public class AuthenticationController {
    @PostMapping("/login")
    @ResponseBody
    public AuthResponse login(@RequestBody AuthRequest request) {
        return new AuthResponse();
    }
}
