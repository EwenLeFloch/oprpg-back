package com.onepiecerpg.api.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.repository.UtilisateurRepository;
import com.onepiecerpg.api.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UtilisateurRepository utilisateurRepository;

  public JwtAuthenticationFilter(JwtService jwtService, UtilisateurRepository utilisateurRepository) {
    this.jwtService = jwtService;
    this.utilisateurRepository = utilisateurRepository;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String authorizationHeader = request.getHeader("Authorization");

    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

      String token = authorizationHeader.substring(7);
      String email = jwtService.extraireEmail(token);

      if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElse(null);

        if (utilisateur != null && jwtService.estTokenValide(token)) {
          UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            utilisateur.getEmail(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole()))
          );
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
    }

    filterChain.doFilter(request, response);

  }
  
}
