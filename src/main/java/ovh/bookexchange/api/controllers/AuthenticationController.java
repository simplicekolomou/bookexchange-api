package ovh.bookexchange.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.AuthRequest;
import ovh.bookexchange.api.controllers.representations.AuthResponse;
import ovh.bookexchange.api.controllers.representations.RegisterRequest;
import ovh.bookexchange.api.services.EndUserDetailsService;
import ovh.bookexchange.api.services.JwtTokenService;

@RestController
public class AuthenticationController {

    public AuthenticationController(AuthenticationManager authenticationManager, EndUserDetailsService endUserDetailsService, JwtTokenService jwtTokenService) {
        this.authenticationManager = authenticationManager;
        this.endUserDetailsService = endUserDetailsService;
        this.jwtTokenService = jwtTokenService;
    }

    private final AuthenticationManager authenticationManager;
    private final EndUserDetailsService endUserDetailsService;
    private final JwtTokenService jwtTokenService;
    @PostMapping("/login")
    @ResponseBody
    public AuthResponse login(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect username or password");
        }
        UserDetails userDetails = endUserDetailsService.loadUserByUsername(request.getEmail());
        AuthResponse response = new AuthResponse();
        response.setAccessToken(jwtTokenService.generateToken(userDetails));
        return response;
    }

    @PostMapping("/register")
    @ResponseBody
    public AuthResponse register(@RequestBody RegisterRequest request) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Register not implemented yet");
    }
}
