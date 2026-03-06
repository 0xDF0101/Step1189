package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // 일단 꺼놓음
                .headers(headers -> headers
                        .frameOptions(options -> options.disable())
                )

                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/", "/h2-console/**", "/login", "signup").permitAll() // 이 경로에만 로그인 없이 접근 가능
                        .requestMatchers("/", "/h2-console/**").permitAll() // 이 경로에만 로그인 없이 접근 가능
                        .anyRequest().authenticated() // 다른 요청들은 로그인 필요
                )
                .formLogin(form -> form
//                        .loginPage("/login")
                        .defaultSuccessUrl("/main")
                )
                .oauth2Login(oauth2 -> oauth2
//                        .loginPage("/login")
                        .defaultSuccessUrl("/main") // OAuth로 로그인 성공시 보여줄 페이지
                );

        return http.build();
    }
}
