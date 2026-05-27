package com.onepiecerpg.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.onepiecerpg.api.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  
  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
      .csrf(AbstractHttpConfigurer::disable)
      .formLogin(AbstractHttpConfigurer::disable)
      .httpBasic(AbstractHttpConfigurer::disable)
      .sessionManagement(session -> 
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .authorizeHttpRequests(auth -> 
        auth.requestMatchers(
          "/api/auth/**",
          "/swagger-ui.html",
          "/swagger-ui/**",
          "/v3/api-docs",
          "/v3/api-docs/**"
        )
        .permitAll()
        .anyRequest().authenticated()
      )
      .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
      .build();
  }
  
  @Bean
  public PasswordEncoder passwordEncoder() {
  return new BCryptPasswordEncoder();  }
}
