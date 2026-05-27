package com.onepiecerpg.api.dto;

public record ConnexionResponse(
  String token,
  String type,
  String pseudo,
  String role
) {
  
}