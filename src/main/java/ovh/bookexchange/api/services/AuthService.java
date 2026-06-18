package ovh.bookexchange.api.services;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.TokenType;
import ovh.bookexchange.api.controllers.representations.UserRep;
import ovh.bookexchange.api.controllers.requestsResponses.*;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final EndUserDetailsService endUserDetailsService;
    private final JwtTokenService jwtTokenService;
    private final EndUserRepository endUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResendMailService emailService;
    private final CookieService cookieService;
    private final ModelMapper mapper;

    // Login
    public UserRep login(AuthRequest request, HttpServletResponse response) {
        EndUser user = authenticate(request.getEmail(), request.getPassword());
        issueAuthCookie(user, response);
        return mapper.map(user, UserRep.class);
    }

    // Register
    public UserRep register(RegisterRequest request, HttpServletResponse response) {
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        EndUser endUser = new EndUser(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                hashedPassword
        );
        try {
            endUserRepository.save(endUser);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        // authentifie après inscription
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail(request.getEmail());
        authRequest.setPassword(request.getPassword());
        return login(authRequest, response);
    }

    // Logout — efface le cookie
    public void logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookieService.clearAuthCookie().toString());
    }

    // Me — retourne le user connecté
    public UserRep getMe(Principal principal) {
        return mapper.map(findUserOr500(principal), UserRep.class);
    }

    // Forgot password
    public void forgotPassword(ForgotPasswordRequest request) {
        EndUser endUser = findUserByEmail(request.getEmail());

        try {
            UserDetails userDetails = endUserDetailsService.loadUserByUsername(endUser.getEmail());
            String token = jwtTokenService.generateToken(userDetails, TokenType.RESET_PASSWORD_TOKEN);
            emailService.sendResetPasswordMail(endUser.getEmail(), token);
        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating reset token");
        }
    }

    // Reset password
    public UserRep resetPassword(ResetPasswordRequest request, HttpServletResponse response) {
        DecodedJWT decodedJWT = jwtTokenService.validateToken(request.getToken());

        // validation du token avant d'utiliser le subject
        if (decodedJWT == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token");
        }

        String email = endUserDetailsService
                .loadUserByUsernameAndToken(decodedJWT.getSubject(), true)
                .getUsername();

        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token subject");
        }

        EndUser endUser = findUserByEmail(email);

        endUser.setPassword(passwordEncoder.encode(request.getPassword()));
        endUserRepository.save(endUser);

        // connecte l'utilisateur après reset
        issueAuthCookie(endUser, response);
        return mapper.map(endUser, UserRep.class);
    }

    // Update password
    public void updatePassword(UpdatePasswordRequest request, Principal principal) {
        EndUser endUser = findUserOr500(principal);

        if (!passwordEncoder.matches(request.getCurrentPassword(), endUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Old password is incorrect");
        }

        endUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        endUserRepository.save(endUser);
    }

    // authentifie et retourne le user — lève 401 si credentials incorrects
    private EndUser authenticate(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect username or password");
        }
        return findUserByEmail(email);
    }

    // génère et pose le cookie httpOnly
    private void issueAuthCookie(EndUser user, HttpServletResponse response) {
        UserDetails userDetails = endUserDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtTokenService.generateToken(userDetails, TokenType.AUTH_TOKEN);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieService.createAuthCookie(token).toString());
    }

    private EndUser findUserOr500(Principal principal) {
        return endUserRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));
    }

    private EndUser findUserByEmail(String email) {
        return endUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));
    }

    public String getWsToken(Principal principal) {
        EndUser user = findUserOr500(principal);
        UserDetails userDetails = endUserDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtTokenService.generateToken(userDetails, TokenType.WS_TOKEN);
        System.out.println("Le token WS pour " + user.getEmail() + " est : " + token);
        return token;
    }

    public String getAccessToken(HttpServletRequest request) {
        String token = null;

        // Récupérer les cookies depuis la requête
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("auth_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token not found in cookies");
        }

        return token;
    }
}