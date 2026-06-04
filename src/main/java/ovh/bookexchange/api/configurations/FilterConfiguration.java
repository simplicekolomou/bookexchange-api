package ovh.bookexchange.api.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import ovh.bookexchange.api.filters.PageSizeFilter;

@Configuration
public class FilterConfiguration {
    @Autowired
    private Environment environment;

    @Bean
    public FilterRegistrationBean<PageSizeFilter> pageSizeFilter() {
        int maxPageSize = environment.getProperty("PAGE_SIZE_MAX", Integer.class, 100);
        int defaultPageSize = environment.getProperty("PAGE_SIZE_DEFAULT", Integer.class, 10);
        FilterRegistrationBean<PageSizeFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new PageSizeFilter(maxPageSize, defaultPageSize));
        return registration;
    }
}
