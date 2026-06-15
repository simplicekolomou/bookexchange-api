package ovh.bookexchange.api.configurations;

import jakarta.persistence.EntityManagerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ovh.bookexchange.api.infrastructures.images.ImageStore;
import ovh.bookexchange.api.services.ResendMailService;

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
        dsBuilder.url(environment.getProperty("DB_URL"));
        dsBuilder.driverClassName(environment.getProperty("DB_DRIVER_CLASS_NAME"));
        dsBuilder.username(environment.getProperty("DB_USERNAME"));
        dsBuilder.password(environment.getProperty("DB_PASSWORD"));
        return dsBuilder.build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(environment.getProperty("DB_DDL_AUTO", Boolean.class, false));
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

    /**
     * Configure le JavaMailSender pour utiliser SendGrid SMTP
     * SendGrid SMTP est un service d'envoi d'e-mails transactionnels qui propose une API SMTP
     * On utilise la formule gratuite qui permet d'envoyer jusqu'à 200 e-mails par jour
     * Pour utiliser SendGrid SMTP, il faut créer un compte SendGrid et générer une API Key
     * La documentation officielle de SendGrid SMTP est disponible ici : <a href="https://www.twilio.com/docs/sendgrid/for-developers/sending-email/integrating-with-the-smtp-api">...</a>
     * SendGrid nécessite une authentification avec un nom d'utilisateur et un mot de passe (API Key)
     * @return JavaMailSender configuré pour SendGrid SMTP
     */
    /*@Bean
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
    }*/

    @Bean
    ResendMailService emailService() {
        return new ResendMailService(environment);
    }
}
