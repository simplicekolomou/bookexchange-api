package ovh.bookexchange.api.configurations;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;

import java.io.FileInputStream;

public class FirebaseConfig {
    @Bean
    public FirebaseApp initFirebase() throws Exception {
        try(FileInputStream serviceAccount = new FileInputStream("../../../firebase/firebase-service-account.json")){
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            return FirebaseApp.initializeApp(options);
        }
    }
}
