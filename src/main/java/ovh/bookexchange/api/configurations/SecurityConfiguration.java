package ovh.bookexchange.api.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ovh.bookexchange.api.services.EndUserDetailsService;
import ovh.bookexchange.api.services.JwtTokenService;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    private JwtRequestFilter jwtRequestFilter(final EndUserDetailsService endUserDetailsService,
                                             final JwtTokenService jwtTokenService) {
        return new JwtRequestFilter(jwtTokenService, endUserDetailsService);
    }

    @Bean
    public SecurityFilterChain configure(final HttpSecurity http,
                                         final EndUserDetailsService endUserDetailsService,
                                         final JwtTokenService jwtTokenService,
                                         Environment environment) throws Exception {
        boolean devSercurityFilters = environment.getProperty("dev.security.filters", Boolean.class, false);
        if (!devSercurityFilters) {
            return http.cors(withDefaults())
                    .csrf(AbstractHttpConfigurer::disable) //TODO: enable CSRF protection (needs frontend and backend implementation)
                    .authorizeHttpRequests((authorize) -> authorize
                            .requestMatchers("/", "/login", "/register").permitAll()
                            .requestMatchers("/error").hasAuthority(EndUserDetailsService.ADMIN)
                            .anyRequest().hasAuthority(EndUserDetailsService.USER))
                    .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .addFilterBefore(jwtRequestFilter(endUserDetailsService, jwtTokenService), UsernamePasswordAuthenticationFilter.class)
                    .build();
        }
        return http.cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/", "/login", "/register").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().hasAuthority(EndUserDetailsService.USER))
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter(endUserDetailsService, jwtTokenService), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
