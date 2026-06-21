package ovh.bookexchange.api.configurations;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    private final Environment environment;

    public FirebaseConfig(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void initializeFirebase() throws Exception {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream credentialsStream = null;

            // 1. Variable FIREBASE_SERVICE_ACCOUNT_JSON (production)
            String jsonContent = environment.getProperty("FIREBASE_SERVICE_ACCOUNT_JSON");
            if (StringUtils.hasText(jsonContent)) {
                credentialsStream = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
                System.out.println("Utilisation de la variable FIREBASE_SERVICE_ACCOUNT_JSON");
            }

            // 2. Fichier dans le dossier "firebase" à la racine du projet (développement)
            if (credentialsStream == null) {
                try {
                    String path = System.getProperty("user.dir") + "/firebase/firebase-service-account.json";
                    credentialsStream = new FileInputStream(path);
                    System.out.println("Utilisation du fichier firebase/firebase-service-account.json à la racine du projet");
                } catch (Exception ignored) {
                    // ignorer
                }
            }

            // 3. Fichier dans le classpath (développement alternatif)
            if (credentialsStream == null) {
                try {
                    credentialsStream = new ClassPathResource("firebase-service-account.json").getInputStream();
                    System.out.println("Utilisation du fichier firebase-service-account.json dans le classpath");
                } catch (Exception ignored) {
                    // ignorer
                }
            }

            // 4. Fichier à la racine du projet (développement alternatif)
            if (credentialsStream == null) {
                try {
                    String path = System.getProperty("user.dir") + "/firebase-service-account.json";
                    credentialsStream = new FileInputStream(path);
                    System.out.println("Utilisation du fichier firebase-service-account.json à la racine du projet");
                } catch (Exception ignored) {
                    // ignorer
                }
            }

            // 5. Aucune source trouvée → exception explicite
            if (credentialsStream == null) {
                throw new IllegalStateException(
                        "Impossible d'initialiser Firebase : aucune source de credentials trouvée.\n" +
                                "Options disponibles :\n" +
                                "  - Placer le fichier firebase-service-account.json dans le dossier 'firebase' à la racine du projet.\n" +
                                "  - Placer le fichier dans src/main/resources (classpath).\n" +
                                "  - Placer le fichier à la racine du projet.\n" +
                                "  - Définir la variable d'environnement FIREBASE_SERVICE_ACCOUNT_JSON avec le contenu brut du JSON.\n" +
                                "  - Définir la variable d'environnement GOOGLE_APPLICATION_CREDENTIALS pointant vers le fichier."
                );
            }

            // 6. Initialisation avec les credentials trouvés
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();
            FirebaseApp.initializeApp(options);
            System.out.println("FirebaseApp initialisée avec succès !");
        }
    }
}