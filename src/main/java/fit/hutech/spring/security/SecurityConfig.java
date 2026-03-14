package fit.hutech.spring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/register", "/login", "/auth/social/**", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/books/add", "/books/add/**", "/books/edit/**", "/books/delete/**").hasRole("ADMIN")
                .requestMatchers("/categories/**").hasRole("ADMIN")
                .requestMatchers("/admin/payment-qr/**").hasRole("ADMIN")
                .requestMatchers("/orders/add", "/orders/add/**", "/orders/edit/**", "/orders/delete/**", "/orders/*/status").hasRole("ADMIN")
                .requestMatchers("/orders/**").authenticated()
                .requestMatchers("/chat/**").authenticated()
                .requestMatchers("/cart/**").hasRole("CUSTOMER")
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .rememberMe(remember -> remember
                .userDetailsService(customUserDetailsService)
                .key("remember-me-key")
                .tokenValiditySeconds(7 * 24 * 60 * 60)
                .rememberMeParameter("remember-me")
            )
            .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
