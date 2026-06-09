package com.onepiecerpg.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onepiecerpg.api.dto.ZoneResponse;
import com.onepiecerpg.api.service.ZoneService;

@RestController
@RequestMapping("/api/zones")
public class ZoneController {

  private final ZoneService zoneService;

  public ZoneController(ZoneService zoneService) {
    this.zoneService = zoneService;
  }

  @GetMapping
  public ResponseEntity<List<ZoneResponse>> recupererToutesLesZones() {
    return ResponseEntity.ok(
        zoneService.recupererToutesLesZones()
            .stream()
            .map(ZoneResponse::from)
            .toList());
  }

  @GetMapping("/{zoneId}")
  public ResponseEntity<ZoneResponse> recupererZoneParId(@PathVariable Long zoneId) {
    return ResponseEntity.ok(ZoneResponse.from(zoneService.recupererZoneParId(zoneId)));
  }

  @GetMapping("/ile/{ileId}")
  public ResponseEntity<List<ZoneResponse>> recupererZonesParIle(@PathVariable Long ileId) {
    return ResponseEntity.ok(
        zoneService.recupererZonesParIle(ileId)
            .stream()
            .map(ZoneResponse::from)
            .toList());
  }
}