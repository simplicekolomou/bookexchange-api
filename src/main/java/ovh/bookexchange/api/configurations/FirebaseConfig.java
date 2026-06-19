package ovh.bookexchange.api.configurations;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() throws Exception {
        if (FirebaseApp.getApps().isEmpty()) {
            // 1. Essayer de lire depuis la variable d'environnement
            String jsonContent = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
            if (StringUtils.hasText(jsonContent)) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(inputStream))
                        .build();
                FirebaseApp.initializeApp(options);
                System.out.println("✅ FirebaseApp initialisée via variable d'environnement FIREBASE_SERVICE_ACCOUNT_JSON");
                return;
            }

            // 2. Fallback : tenter de charger depuis le classpath (si le fichier est présent)
            try {
                // Vous pouvez garder un fallback si le fichier existe en local
                // ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
                // ... initialisation avec resource.getInputStream()
            } catch (Exception ignored) {}

            // 3. Fallback ultime : utiliser GOOGLE_APPLICATION_CREDENTIALS si définie
            FirebaseApp.initializeApp();
            System.out.println("✅ FirebaseApp initialisée via GOOGLE_APPLICATION_CREDENTIALS (ou par défaut)");
        }
    }
}