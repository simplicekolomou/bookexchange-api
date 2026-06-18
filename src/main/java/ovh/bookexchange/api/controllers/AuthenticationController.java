package ovh.bookexchange.api.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ovh.bookexchange.api.controllers.representations.UserRep;
import ovh.bookexchange.api.controllers.requestsResponses.*;
import ovh.bookexchange.api.services.AuthService;

import java.security.Principal;

@RestController
public class AuthenticationController {

    private final AuthService authService;

    public AuthenticationController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserRep> login(@Valid @RequestBody AuthRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/register")
    public ResponseEntity<UserRep> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request, response));
    }

    @GetMapping("/ws-token")
    public String getWebSocketToken(Principal principal) {
        return authService.getWsToken(principal);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.noContent().build();
    }

    // endpoint GET /me pour hydrater le store au refresh
    @GetMapping("/me")
    public ResponseEntity<UserRep> me(Principal principal) {
        return ResponseEntity.ok(authService.getMe(principal));
    }

    @PutMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<UserRep> resetPassword(@RequestBody ResetPasswordRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.resetPassword(request, response));
    }

    @PutMapping("/update-password")
    public ResponseEntity<Void> updatePassword(@RequestBody UpdatePasswordRequest request, Principal principal) {
        authService.updatePassword(request, principal);
        return ResponseEntity.noContent().build();
    }
}
