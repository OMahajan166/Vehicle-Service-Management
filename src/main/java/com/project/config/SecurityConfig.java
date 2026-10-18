package com.project.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider auth =
                new DaoAuthenticationProvider();

        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(passwordEncoder());

        return auth;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authenticationProvider(
                        authenticationProvider())

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/",
                                "/auth/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/contact",
                                "/about",
                                "/services")
                        .permitAll()

                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/customer/**")
                        .hasRole("CUSTOMER")

                        .requestMatchers("/mechanic/**")
                        .hasRole("MECHANIC")

                        .anyRequest()
                        .authenticated())

                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .successHandler((request,
                                response,
                                authentication) -> {

                   CustomUserDetails user =
                           (CustomUserDetails)
                                   authentication.getPrincipal();

                   if ("ADMIN".equals(user.getRole())) {

                       response.sendRedirect(
                               "/admin/dashboard");

                   } else if ("MECHANIC".equals(user.getRole())) {

                       response.sendRedirect(
                               "/mechanic/dashboard");

                   } else {

                       response.sendRedirect(
                               "/customer/dashboard");
                   }
               })
                        .permitAll())

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll());

        return http.build();
    }
}