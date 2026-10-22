package com.restaurante.sistema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança.
 *
 * "/api/usuarios/**" e "/api/admin/**" estão liberados aqui no
 * filtro (permitAll), mas isso NÃO significa acesso livre — cada
 * controller confere manualmente, via HttpSession, se existe login
 * válido e (no caso do admin) se o papel é o correto.
 *
 * "/administrador" e "/administrador/painel" são públicas no filtro
 * pela mesma razão de sempre: senão a própria página não carregaria.
 * A proteção de verdade acontece client-side (administrador-painel.js
 * redireciona se a API retornar 401/403) e, principalmente,
 * server-side, no AdminController.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                .requestMatchers(
                        "/cadastro", "/entrar", "/painel", "/perfil",
                        "/administrador", "/administrador/painel",
                        "/redefinir-senha", "/redefinir-senha/confirmar"
                ).permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/usuarios/**").permitAll()
                .requestMatchers("/api/admin/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}