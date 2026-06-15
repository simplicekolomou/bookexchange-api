package ovh.bookexchange.api.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;

@Service
public class FirebaseService {
    private final EndUserRepository userRepository;

    public FirebaseService(EndUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Envoie une notification push à un utilisateur spécifique.
     * @param token Le token FCM de l'appareil cible.
     * @param title Le titre de la notification.
     * @param body  Le corps de la notification.
     * @throws Exception Si l'envoi échoue.
     */
    public void sendNotificationToToken(String token, String title, String body) throws Exception {
        // Construire la notification
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        // Construire le message avec le token cible
        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .build();

        // Envoyer le message de manière asynchrone et attendre le résultat
        String response = FirebaseMessaging.getInstance().sendAsync(message).get();
        System.out.println("Message FCM envoyé avec succès : " + response);
    }

    public void saveFcmToken(String email, String fcmToken){
        EndUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'email : " + email));

        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }
}
