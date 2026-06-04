/**
 * Service de gestion des e-mails pour l'application BookExchange.
 * Ce service utilise la bibliothèque Resend pour envoyer des e-mails de réinitialisation de mot de passe.
 * Il est configuré pour envoyer des e-mails à partir d'une adresse spécifiée dans les propriétés de l'application.
 * Il construit le contenu de l'e-mail en incluant un lien de réinitialisation qui contient un token unique.
 * Le service gère également les exceptions liées à l'envoi d'e-mails et enregistre les erreurs dans les logs.
 * Les propriétés nécessaires pour ce service sont :
 * - RESEND_MAIL_APIKEY : la clé API pour accéder au service Resend
 * - MAIL_FROM : l'adresse e-mail à partir de laquelle les e-mails seront envoyés
 * - RESET_LINK_BASE_URL : l'URL de base pour le lien de réinitialisation, à laquelle le token sera ajouté
 * Note TEST : Avec le plan gratuit de Resend, il n'est pas possible d'envoyer des emails à une autre adresse
 * mail autre que celle utilisée lors de la création de votre compte Resend. Assurez-vous que l'adresse e-mail
 * utilisée pour envoyer les e-mails de réinitialisation est la même que celle associée à votre compte Resend.
 */
package ovh.bookexchange.api.services;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public class ResendMailService {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);
    private final Resend resend;
    private final String fromEmail;
    private final String resetLink;

    public ResendMailService(Environment env) {
        String apiKey = env.getProperty("RESEND_MAIL_APIKEY");
        this.resend = new Resend(apiKey);
        this.fromEmail = env.getProperty("MAIL_FROM");
        this.resetLink = env.getProperty("RESET_LINK_BASE_URL");
    }

    public void sendResetPasswordMail(String to, String token) {
        String fullResetLink = resetLink + token;
        String emailBody = "Réinitialisation du mot de passe - BookExchange,\n\n"
                + "Cliquez sur ce lien pour réinitialiser votre mot de passe :\n"
                + fullResetLink;

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(to)
                .subject("Réinitialisation de votre mot de passe")
                .text(emailBody)      // version texte brut
                // .html("<p>...</p>") // si vous préférez du HTML
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            log.info("Email de réinitialisation envoyé à {} (id : {})", to, response.getId());
        } catch (ResendException e) {
            log.error("Échec d'envoi de l'email à {} : {}", to, e.getMessage());
        }
    }
}
