package com.onepiecerpg.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onepiecerpg.api.dto.EnnemiResponse;
import com.onepiecerpg.api.service.EnnemiService;

@RestController
@RequestMapping("/api/ennemis")
public class EnnemiController {

    private final EnnemiService ennemiService;

    public EnnemiController(EnnemiService ennemiService) {
        this.ennemiService = ennemiService;
    }

    @GetMapping("/{ennemiId}")
    public ResponseEntity<EnnemiResponse> recupererEnnemiParId(@PathVariable Long ennemiId) {
        return ResponseEntity.ok(EnnemiResponse.from(ennemiService.recupererEnnemiParId(ennemiId)));
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<EnnemiResponse>> recupererEnnemisParZone(@PathVariable Long zoneId) {
        return ResponseEntity.ok(
                ennemiService.recupererEnnemisParZone(zoneId)
                        .stream()
                        .map(EnnemiResponse::from)
                        .toList()
        );
    }

    @GetMapping("/zone/{zoneId}/classiques")
    public ResponseEntity<List<EnnemiResponse>> recupererEnnemisClassiquesParZone(@PathVariable Long zoneId) {
        return ResponseEntity.ok(
                ennemiService.recupererEnnemisClassiquesParZone(zoneId)
                        .stream()
                        .map(EnnemiResponse::from)
                        .toList()
        );
    }
}