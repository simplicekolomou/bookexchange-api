package ovh.bookexchange.api.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;


@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);
    private final MailSender mailSender;
    private final SimpleMailMessage templateMessage;

    @Autowired
    @Lazy
    public EmailService(MailSender mailSender, SimpleMailMessage templateMessage) {
        this.mailSender = mailSender;
        this.templateMessage = templateMessage;
    }

    public void sendResetPasswordMail(String to, String token) {
        SimpleMailMessage msg = new SimpleMailMessage(templateMessage);
        msg.setTo(to);
        String resetLink = "http://localhost:5173/reset-password?token=" + token;
        msg.setText(
                "Réinitialisation du mot de passe," +
                        "Cliquez sur ce lien pour réinitialiser votre mot de passe : " + resetLink
        );

        try {
            mailSender.send(msg);
        } catch (MailException ex) {
            log.error("Erreur lors de l'envoi de l'e-mail de réinitialisation du mot de passe à {} : {}", to, ex.getMessage());
        }
    }

    /**
     * Configure le JavaMailSender pour utiliser SendGrid SMTP
     * SendGrid SMTP est un service d'envoi d'e-mails transactionnels qui propose une API SMTP
     * On utilise la formule gratuite qui permet d'envoyer jusqu'à 200 e-mails par jour
     * Pour utiliser SendGrid SMTP, il faut créer un compte SendGrid et générer une API Key
     * La documentation officielle de SendGrid SMTP est disponible ici : https://www.twilio.com/docs/sendgrid/for-developers/sending-email/integrating-with-the-smtp-api
     * SendGrid nécessite une authentification avec un nom d'utilisateur et un mot de passe (API Key)
     * @return JavaMailSender configuré pour SendGrid SMTP
     */
    @Bean
    JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.sendgrid.net");
        mailSender.setPort(587);
        mailSender.setUsername("apikey");
        mailSender.setPassword("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");
        return mailSender;
    }

    @Bean
    SimpleMailMessage templateMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("jsktresor@gmail.com");
        message.setSubject("Réinitialisation de mot de passe - BookExchange");
        return message;
    }
}