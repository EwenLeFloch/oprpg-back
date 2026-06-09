package com.onepiecerpg.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onepiecerpg.api.dto.IleResponse;
import com.onepiecerpg.api.service.IleService;

@RestController
@RequestMapping("/api/iles")
public class IleController {

  private final IleService ileService;

  public IleController(IleService ileService) {
    this.ileService = ileService;
  }

  @GetMapping
  public ResponseEntity<List<IleResponse>> recupererToutesLesIles() {
    return ResponseEntity.ok(
        ileService.recupererToutesLesIles()
            .stream()
            .map(IleResponse::from)
            .toList());
  }

  @GetMapping("/{ileId}")
  public ResponseEntity<IleResponse> recupererIleParId(@PathVariable Long ileId) {
    return ResponseEntity.ok(IleResponse.from(ileService.recupererIleParId(ileId)));
  }
}