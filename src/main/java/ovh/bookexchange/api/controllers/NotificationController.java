package ovh.bookexchange.api.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ovh.bookexchange.api.services.FirebaseService;

import java.security.Principal;
import java.util.Map;

@RestController()
@RequestMapping("/notifications")
@Slf4j
public class NotificationController {
    private final FirebaseService firebaseService;

    public NotificationController(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @PostMapping("/register-token")
    public ResponseEntity<?> registerFcmToken(Principal principal, @RequestBody Map<String, String> payload) {
        String fcmToken = payload.get("token");
        System.out.println("Enregistrement du token FCM pour l'utilisateur " + principal.getName() + ": " + fcmToken);
        if (fcmToken == null || fcmToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Token manquant");
        }
        String email = principal.getName();
        firebaseService.saveFcmToken(email, fcmToken);
        return ResponseEntity.ok().build();
    }
}
