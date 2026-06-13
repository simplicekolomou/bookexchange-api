package ovh.bookexchange.api.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class SendGridMailService {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);
    private final MailSender mailSender;
    private final SimpleMailMessage templateMessage;

    private final String resetLink;

    public SendGridMailService(MailSender mailSender, String resetLink, String from, String subject) {
        this.mailSender = mailSender;
        this.resetLink = resetLink;
        this.templateMessage = templateMessage(from, subject);
    }

    public void sendResetPasswordMail(String to, String token) {
        SimpleMailMessage msg = new SimpleMailMessage(templateMessage);
        msg.setTo(to);
        String resetLink = this.resetLink + token;
        msg.setText(
                "Réinitialisation du mot de passe," +
                        "Cliquez sur ce lien pour réinitialiser votre mot de passe : " + resetLink
        );

        try {
            mailSender.send(msg);
            log.info("E-mail de réinitialisation du mot de passe envoyé à : {}", to);
        } catch (MailException ex) {
            log.error("Erreur lors de l'envoi de l'e-mail de réinitialisation du mot de passe à {} : {}", to, ex.getMessage());
        }
    }


    private static SimpleMailMessage templateMessage(String from, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setSubject(subject);
        return message;
    }
}