package ovh.bookexchange.api.services;

import lombok.extern.slf4j.Slf4j;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.domains.entities.notifications.NotifSub;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Service for sending web push notifications, calls the python script push.py in python_src folder.
 * Relies on a venv beeing present in a "venv" folder at project root.
 * Was written on linux, for linux. Untested on other platforms.
 */
@Slf4j
public class NotificationService {

    public NotificationService(EndUserRepository endUserRepository, String vapidPublicKey, String vapidPrivateKey, String vapidClaim) {
        this.endUserRepository = endUserRepository;
        this.vapidPrivateKey = Objects.requireNonNull(vapidPrivateKey);
        this.vapidPublicKey = Objects.requireNonNull(vapidPublicKey);
        this.vapidClaim = Objects.requireNonNull(vapidClaim);
    }

    private final EndUserRepository endUserRepository;
    private final String vapidPublicKey;
    private final String vapidPrivateKey;
    private final String vapidClaim;

    /**
     * Sends a notification to a user, using the python script mentionned in class description.
     * If sending the notification fails, logs the error, should not throw an exception.
     * @param title The title of the notification
     * @param body The body of the notification
     * @param email The email of the user to send the notification to
     * @throws IllegalArgumentException if no users exists with the given email.
     */
    public void sendNotification(String title, String body, String email) {
        try {
            EndUser user = endUserRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("No user found with email: " + email ));
            NotifSub notifSub = user.getNotifSub();
            if (notifSub == null) {
                throw new IllegalArgumentException("User has no subscription");
            }
            log.info("Sending notification to {}", email);
            ProcessBuilder pb = new ProcessBuilder();
            pb.inheritIO();
            pb.command("./venv/bin/python3", "push.py", notifSub.getEndpoint(), notifSub.getKeys().getAuth(), notifSub.getKeys().getP256dh(), body, title, vapidPublicKey, vapidPrivateKey, vapidClaim);
            Process p = pb.start();
            int responseStatus = 0;
            boolean inReasonableTime = p.waitFor(180, TimeUnit.SECONDS);
            if (inReasonableTime) {
                responseStatus = p.exitValue();
            }
            if (!inReasonableTime || responseStatus != 0) {
                log.error("Error sending notification, inReasonableTime: {}, responseStatus: {}", inReasonableTime, responseStatus);
            }

        } catch (InterruptedException | NullPointerException | IOException e) {
            log.error("Error sending notification", e);
        }
    }

    /**
     * Subscribe a user to notifications.
     * @param email the email of the user to subscribe
     * @param notifSub the subscription to save, as give by the browser's pushManager.subscribe() method.
     */
    public void subscribeUser(String email, NotifSub notifSub) {
        EndUser user = endUserRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("No user found with email: " + email ));
        user.setNotifSub(notifSub);
        endUserRepository.save(user);
    }
}
