package ca.carleton.s4806.perkmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class that provides application-wide security utilities.
 *
 * This defines a PasswordEncoder bean that uses BCrypt hashing.
 * BCrypt is a secure algorithm for storing passwords because it uses
 * salting and multiple rounds of hashing.
 *
 * Any class that needs to hash or verify passwords can inject this bean.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
