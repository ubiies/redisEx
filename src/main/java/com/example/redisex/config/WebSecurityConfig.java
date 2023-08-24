package com.example.redisex.config;

import com.example.redisex.global.jwt.JwtTokenFilter;
import com.example.redisex.global.jwt.JwtTokenProvider;
import com.example.redisex.global.jwt.JwtTokenUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class WebSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    private JwtTokenProvider jwtTokenProvider;
    private final JwtTokenUtils jwtTokenUtils;

    public WebSecurityConfig(JwtTokenFilter jwtTokenFilter, JwtTokenProvider jwtTokenProvider, JwtTokenUtils jwtTokenUtils) {
        this.jwtTokenFilter = jwtTokenFilter;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtTokenUtils = jwtTokenUtils;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authHttpRequest -> authHttpRequest
                        .anyRequest()
                        .permitAll())
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        // jwt 적용
        http.apply(new JwtSecurityConfig(jwtTokenProvider,jwtTokenUtils));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager를 설정하고 반환하는 역할
    // Spring Security의 설정을 보완하거나 재정의하는 경우 사용
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
