package org.example.config;

import lombok.RequiredArgsConstructor;
import org.example.controller.api.OAuth2SuccessHandler;
import org.example.service.auth.CustomUserDetailService;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailService userDetailService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, OAuth2ClientProperties oAuth2ClientProperties, OAuth2SuccessHandler oAuth2SuccessHandler) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // 일단 꺼놓음
                .headers(headers -> headers
                        .frameOptions(options -> options.disable())
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/",
                                "/h2-console/**",
                                "/login",
                                "/signup",
                                "/api/v1/users",
                                "/signup/set-username").permitAll() // 이 경로에만 로그인 없이 접근 가능
//                        .requestMatchers("/", "/h2-console/**").permitAll() // 이 경로에만 로그인 없이 접근 가능
                        .anyRequest().authenticated() // 다른 요청들은 로그인 필요
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/main")
                )
                .userDetailsService(userDetailService)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
//                        .defaultSuccessUrl("/main") // OAuth로 로그인 성공시 보여줄 페이지
                        .successHandler(oAuth2SuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

