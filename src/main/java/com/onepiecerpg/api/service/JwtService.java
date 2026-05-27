package com.onepiecerpg.api.service;

import org.springframework.stereotype.Service;

import com.onepiecerpg.api.entity.Utilisateur;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;


@Service
public class JwtService {
  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private long expiration;

  public String genererToken(Utilisateur utilisateur) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .subject(utilisateur.getEmail())
        .claim("pseudo", utilisateur.getPseudo())
        .claim("role", utilisateur.getRole())
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(getSigningKey())
        .compact();
  }

  public String extraireEmail(String token) {
    return extraireClaims(token).getSubject();
  }

  public boolean estTokenValide(String token) {
    return extraireClaims(token).getExpiration().after(new Date());
  }

  private Claims extraireClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }
}
