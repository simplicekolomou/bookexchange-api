package ovh.bookexchange.api;

import jakarta.persistence.EntityManagerFactory;
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

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
public class ApiConfiguration {
    @Autowired
    private Environment environment;

    @Bean
    public DataSource dataSource() {
        DataSourceBuilder dsBuilder = DataSourceBuilder.create();
        dsBuilder.url(environment.getProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/postgres"));
        dsBuilder.driverClassName(environment.getProperty("spring.datasource.driver-class-name","org.postgresql.Driver"));
        dsBuilder.username(environment.getProperty("spring.datasource.username", "postgres"));
        dsBuilder.password(environment.getProperty("spring.datasource.password", "password"));
        return dsBuilder.build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(environment.getProperty("spring.jpa.hibernate.ddl-auto", Boolean.class, false));

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
}
