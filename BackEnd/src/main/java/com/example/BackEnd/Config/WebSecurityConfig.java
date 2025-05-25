package com.example.BackEnd.Config;

import com.example.BackEnd.Entity.Role;
import com.example.BackEnd.Filter.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity()
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    @Value("${api.prefix}")
    private String apiPrefix;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                                String.format("%s/users/login", apiPrefix),
                                String.format("%s/users/register", apiPrefix),
                                String.format("%s/users/verify", apiPrefix),
                                String.format("%s/users/retrieve", apiPrefix)
                        ).permitAll()
                        .requestMatchers(GET,
                                String.format("%s/roles**", apiPrefix)).permitAll()

                        .requestMatchers(GET,
                                String.format("%s/images/**", apiPrefix)).permitAll()
                                
                        .requestMatchers(GET,
                                String.format("%s/users", apiPrefix)).hasRole(Role.ADMIN)
                        .requestMatchers(POST,
                                String.format("%s/users/details**", apiPrefix)).permitAll()
                        .requestMatchers(PUT,
                                String.format("%s/users/updates/**", apiPrefix)).permitAll()
                        .requestMatchers(PUT,
                                String.format("%s/users/change-password/**", apiPrefix)).permitAll()
                        .requestMatchers(DELETE,
                                String.format("%s/users/delete", apiPrefix)).hasRole(Role.ADMIN)
                        .requestMatchers(DELETE,
                                String.format("%s/users/logout", apiPrefix)).hasAnyRole(Role.USER, Role.DOCTOR, Role.ADMIN)

                        .requestMatchers(GET,
                                String.format("%s/doctors", apiPrefix)).permitAll()
                        .requestMatchers(GET,
                                String.format("%s/doctors/getByDepartment/**", apiPrefix)).permitAll()
                        .requestMatchers(GET,
                                String.format("%s/doctors/getDoctor/**", apiPrefix)).permitAll()
                        .requestMatchers(POST,
                                String.format("%s/doctors/add**", apiPrefix)).hasRole(Role.ADMIN)
                        .requestMatchers(DELETE,
                                String.format("%s/doctors/delete/**", apiPrefix)).hasRole(Role.ADMIN)

                        .requestMatchers(GET,
                                String.format("%s/departments", apiPrefix)).permitAll()
                        .requestMatchers(POST,
                                String.format("%s/departments/add**", apiPrefix)).hasRole(Role.ADMIN)

                        .requestMatchers(GET,
                                String.format("%s/appointments", apiPrefix)).hasRole(Role.ADMIN)
                        .requestMatchers(GET,
                                String.format("%s/appointments/analyze", apiPrefix)).hasRole(Role.ADMIN)
                        .requestMatchers(GET,
                                String.format("%s/appointments/ByUser/**", apiPrefix)).hasRole(Role.USER)
                        .requestMatchers(GET,
                                String.format("%s/appointments/ByDoctor/**", apiPrefix)).hasRole(Role.DOCTOR)
                        .requestMatchers(GET,
                                String.format("%s/appointments/medical-report/**", apiPrefix)).hasRole(Role.ADMIN)
                        .requestMatchers(POST,
                                String.format("%s/appointments/schedule**", apiPrefix)).hasAnyRole(Role.USER, Role.DOCTOR, Role.ADMIN)
                        .requestMatchers(PUT,
                                String.format("%s/appointments/paid", apiPrefix)).hasRole(Role.ADMIN)
                        .requestMatchers(PUT,
                                String.format("%s/appointments/**", apiPrefix)).hasRole(Role.DOCTOR)

                        .requestMatchers(GET,
                                String.format("%s/reviews", apiPrefix)).permitAll()
                        .requestMatchers(GET,
                                String.format("%s/reviews/analyze", apiPrefix)).hasRole(Role.ADMIN)
                        .requestMatchers(POST,
                                String.format("%s/reviews/feedback**", apiPrefix)).hasAnyRole(Role.USER, Role.DOCTOR, Role.ADMIN)
                        .requestMatchers(DELETE,
                                String.format("%s/reviews/**", apiPrefix)).hasRole(Role.ADMIN)

                        .requestMatchers(GET,
                                String.format("%s/articles/**", apiPrefix)).permitAll()
                        .requestMatchers(POST,
                                String.format("%s/articles/**", apiPrefix)).hasRole(Role.ADMIN)

                        .requestMatchers(GET,
                                String.format("%s/home/list**", apiPrefix)).hasRole(Role.ADMIN)
                        .requestMatchers(POST,
                                String.format("%s/home**", apiPrefix)).hasRole(Role.ADMIN)
                        .requestMatchers(DELETE,
                                String.format("%s/home**", apiPrefix)).hasRole(Role.ADMIN)
                        .anyRequest().authenticated()
                );

        return http.build();
    }

}

