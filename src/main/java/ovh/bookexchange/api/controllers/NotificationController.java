package ovh.bookexchange.api.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.domains.entities.notifications.NotifSub;
import ovh.bookexchange.api.services.NotificationService;

import java.security.Principal;

@RestController()
@RequestMapping("/notifications")
@Slf4j
public class NotificationController {

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private final NotificationService notificationService;

    @PutMapping()
    public void storeNotification(@RequestBody @Valid NotifSub notification, Principal principal) {
        try {
            notificationService.subscribeUser(principal.getName(), notification);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found");
        }
    }

/*
    route qui test un evoi de notification à l'utilisateur qui appel la route.
    @GetMapping void notificationTest(Principal principal) {
        try {
            notificationService.sendNotification("un titre", "le jolly corp de la notification", principal.getName());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }*/


}
