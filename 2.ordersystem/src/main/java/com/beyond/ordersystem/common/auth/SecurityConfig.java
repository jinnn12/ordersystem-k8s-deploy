package com.beyond.ordersystem.common.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    private final JwtAuthenticationHandler jwtAuthenticationHandler;
    private final JwtAuthorizationHandler jwtAuthorizationHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(c -> c.configurationSource(corsConfiguration()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(a ->
                        a.requestMatchers("/member/create",
                                        "/member/doLogin",
                                        "/member/refresh-at",
                                        "/product/list",
                                        "/v3/api-docs/**",
                                        "/health"
                                        )
                                .permitAll().anyRequest().authenticated())
                .exceptionHandling(e ->
                        e.authenticationEntryPoint(jwtAuthenticationHandler)
                                .accessDeniedHandler(jwtAuthorizationHandler))
                .build();
    }

    private CorsConfigurationSource corsConfiguration(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3111", "https://www.jinho2000.shop")); // 원하는 도메인 커스텀
        configuration.setAllowedMethods(Arrays.asList("*")); // 모든 HTTP(get, post, patch 등) 메서드 허용
        configuration.setAllowedHeaders(Arrays.asList("*")); // 모든 헤더요소(Authorization 등) 허용
        configuration.setAllowCredentials(true); // 자격 증명 허용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); //모든 url패턴에 대해 cors설정 적용, ** : 모든계층구조
        return source; // 모든 정책이 들어가 있음
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
