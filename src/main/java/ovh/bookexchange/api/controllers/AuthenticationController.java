package ovh.bookexchange.api.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.UserRep;
import ovh.bookexchange.api.controllers.requestsResponses.*;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;
import ovh.bookexchange.api.services.EmailService;
import ovh.bookexchange.api.services.EndUserDetailsService;
import ovh.bookexchange.api.services.JwtTokenService;

import java.security.Principal;

@RestController
public class AuthenticationController {

    public AuthenticationController(AuthenticationManager authenticationManager, EndUserDetailsService endUserDetailsService, JwtTokenService jwtTokenService, EndUserRepository endUserRepository, PasswordEncoder passwordEncoder, ModelMapper mapper, EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.endUserDetailsService = endUserDetailsService;
        this.jwtTokenService = jwtTokenService;
        this.endUserRepository = endUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.emailService = emailService;
    }

    private final AuthenticationManager authenticationManager;
    private final EndUserDetailsService endUserDetailsService;
    private final JwtTokenService jwtTokenService;

    private final EndUserRepository endUserRepository;

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final ModelMapper mapper;


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
            return getAuthResponse(request.getEmail(), password);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
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
        EndUser endUser = endUserRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));
        response.setUser(mapper.map(endUser, UserRep.class));
        return response;
    }

    /**
     * Envoi un email de réinitialisation de mdp.
     */
    @PutMapping("/forgot-password")
    @ResponseBody
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        createPasswordResetToken(request.getEmail());
    }

    /**
     * Réinitialise le mdp, authentifie l'utilisateur grâce au token contenu dans l'email.
     * Retourne une réponse connectant l'utilisateur.
     */
    @PostMapping("/reset-password")
    public AuthResponse resetPassword(@RequestBody ResetPasswordRequest request) {
        return resetPassword(request.getToken(), request.getPassword());
    }

    @PutMapping("/update-password")
    public void updatePassword(@RequestBody UpdatePasswordRequest request, Principal principal) {
        EndUser endUser = endUserRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.getCurrentPassword(), endUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Old password is incorrect");
        }

        // Mettre à jour avec le nouveau mot de passe
        endUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        endUserRepository.save(endUser);
    }

    private AuthResponse resetPassword(String token, String newPassword) {
        // Valider le token JWT
        DecodedJWT decodedJWT = jwtTokenService.validateToken(token);
        String subject = decodedJWT.getSubject();
        if (decodedJWT == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token");
        }

        String email = endUserDetailsService.loadUserByUsernameAndToken(subject, true).getUsername();

        // Récupérer l'utilisateur à partir de l'email dans le token
        //String email = decodedJWT.getSubject();
        if(email == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token subject");
        }
        EndUser endUser = endUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        // Mettre à jour le mot de passe
        endUser.setPassword(passwordEncoder.encode(newPassword));
        endUserRepository.save(endUser);

        // Connecter l'utilisateur en générant un nouveau token JWT
        return getAuthResponse(email, newPassword);
    }

    /**
     * Crée un token de reset de mdp, et l'envoi par mail..
     * @param email l'email de l'utilisateur dont le mdp doit être réinitialiser.
     * @throws ResponseStatusException si l'utilisateur n'existe pas (NOT_FOUND, soit une 404).
     */
    private void createPasswordResetToken(String email) {
        EndUser endUser = endUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found"));
        try{
            UserDetails userDetails = endUserDetailsService.loadUserByUsername(endUser.getEmail());
            String token = jwtTokenService.generateToken(userDetails, true);
            emailService.sendResetPasswordMail(email, token);
        }catch (ResponseStatusException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating reset token");
        }
    }
}
