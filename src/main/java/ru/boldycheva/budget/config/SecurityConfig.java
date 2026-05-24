package ru.boldycheva.budget.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        // Публичные страницы
                        .requestMatchers("/", "/home", "/css/**", "/js/**", "/images/**", "/login", "/logout").permitAll()
                        .requestMatchers("/ws/**", "/ws/info").permitAll()

                        // Админские страницы
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")

                        // API для Telegram - временно без аутентификации. Надо разбираться почему не работает с ней.
                        .requestMatchers("/api/telegram/**").permitAll()

                        // Все остальные страницы
                        .requestMatchers("/transactions/**", "/accounts/**", "/my-accounts/**").authenticated()
                        .requestMatchers("/transactions/api/**").authenticated()
                        .requestMatchers("/cabinet").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(new HttpSessionSecurityContextRepository())
                        .requireExplicitSave(false)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}