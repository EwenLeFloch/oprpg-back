package com.onepiecerpg.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

  @GetMapping("/api/test/protege")
  public String routeProtegee() {
    return "Accès autorisé à la route protégée";
  }
}
