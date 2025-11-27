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

import javax.sql.DataSource;
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
        return new ImageStore(
                Paths.get(environment.getProperty("profile.picture.folder.path", String.class, "uploads/profile_pictures")),
                Arrays.stream(environment.getProperty("profile.picture.image.formats", String.class, "jpeg;png").split(";")).toList()
        );
    }
}
