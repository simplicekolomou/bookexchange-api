package ovh.bookexchange.api.controllers;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.AuthRequest;
import ovh.bookexchange.api.controllers.representations.AuthResponse;
import ovh.bookexchange.api.controllers.representations.RegisterRequest;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.infrastructures.EndUserRepository;
import ovh.bookexchange.api.services.EndUserDetailsService;
import ovh.bookexchange.api.services.JwtTokenService;

@RestController
public class AuthenticationController {

    public AuthenticationController(AuthenticationManager authenticationManager, EndUserDetailsService endUserDetailsService, JwtTokenService jwtTokenService, EndUserRepository endUserRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.endUserDetailsService = endUserDetailsService;
        this.jwtTokenService = jwtTokenService;
        this.endUserRepository = endUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private final AuthenticationManager authenticationManager;
    private final EndUserDetailsService endUserDetailsService;
    private final JwtTokenService jwtTokenService;

    private final EndUserRepository endUserRepository;

    private final PasswordEncoder passwordEncoder;
    @PostMapping("/login")
    @ResponseBody
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return getAuthResponse(request.getEmail(), request.getPassword());
    }

    @PostMapping("/register")
    @ResponseBody
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        String password = request.getPassword();
        String hashedPassword = passwordEncoder.encode(password);
        EndUser endUser = new EndUser(request.getFirstName(), request.getLastName(), request.getEmail(), hashedPassword);
        try {
            endUserRepository.save(endUser);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        return getAuthResponse(request.getEmail(), password);
    }

    private AuthResponse getAuthResponse(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect username or password");
        }
        UserDetails userDetails = endUserDetailsService.loadUserByUsername(email);
        AuthResponse response = new AuthResponse();
        response.setAccessToken(jwtTokenService.generateToken(userDetails));
        return response;
    }
}
