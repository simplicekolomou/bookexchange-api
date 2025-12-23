package ovh.bookexchange.api.configurations;

import jakarta.persistence.EntityManagerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ovh.bookexchange.api.infrastructures.images.ImageStore;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;
import ovh.bookexchange.api.services.EmailService;
import ovh.bookexchange.api.services.NotificationService;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Configuration
@EnableJpaRepositories(basePackages = "ovh.bookexchange.api.infrastructures.repos")
@EnableTransactionManagement
public class ApiConfiguration {
    @Autowired
    private Environment environment;

    @Bean
    public DataSource dataSource() {
        DataSourceBuilder dsBuilder = DataSourceBuilder.create();
        dsBuilder.url(environment.getProperty("db.url", "jdbc:postgresql://localhost:5432/postgres"));
        dsBuilder.driverClassName(environment.getProperty("db.driver.class.name","org.postgresql.Driver"));
        dsBuilder.username(environment.getProperty("db.username", "postgres"));
        dsBuilder.password(environment.getProperty("db.password", "password"));
        return dsBuilder.build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(environment.getProperty("db.ddl.auto", Boolean.class, false));
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("ovh.bookexchange.api.domains.entities");
        factory.setDataSource(dataSource());
        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper(); //add eventual model mapper configuration
    }

    @Bean
    public ImageStore imageStore() {
        Path imagePath = Paths.get(environment.getProperty("profile.picture.folder.path", String.class, "uploads/profile_pictures"));
        if (!imagePath.toFile().exists()) {
            imagePath.toFile().mkdirs();
        }
        return new ImageStore(
                imagePath,
                Arrays.stream(environment.getProperty("profile.picture.image.formats", String.class, "jpeg;png").split(";")).toList()
        );
    }

    @Bean
    public NotificationService notificationService(EndUserRepository endUserRepository) {
        String privateKey = environment.getProperty("vapid.private.key");
        String publicKey = environment.getProperty("vapid.public.key", "BBDyKyvknQuhwMLj-YhZrpUS6M0ZvVcCZYnm0C9R8Ir_ucJT_JPfUboTsKeCvjnrTPJQ-x2XA-dCzjrw0ONldqs");
        String claim = environment.getProperty("vapid.claim");
        return new NotificationService(endUserRepository, publicKey, privateKey, claim);
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
        mailSender.setHost(environment.getProperty("smtp.host","smtp.sendgrid.net"));
        mailSender.setPort(environment.getProperty("smtp.port",Integer.class,587));
        mailSender.setUsername(environment.getProperty("smtp.username", "apikey"));
        mailSender.setPassword(environment.getProperty("smtp.password"));
        return mailSender;
    }

    @Bean
    EmailService emailService(JavaMailSender mailSender) {
        String resetLink = environment.getProperty("reset.link", "http://localhost:5173/reset-password?token=");
        String from = environment.getProperty("mail.from", "jsktresor@gmail.com");
        String subject = environment.getProperty("mail.subject", "Réinitialisation de mot de passe - BookExchange");
        return new EmailService(mailSender, resetLink, from, subject);
    }
}
