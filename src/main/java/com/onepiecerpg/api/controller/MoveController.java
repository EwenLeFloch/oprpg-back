package com.onepiecerpg.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onepiecerpg.api.dto.MoveResponse;
import com.onepiecerpg.api.entity.TypeMove;
import com.onepiecerpg.api.service.MoveService;

@RestController
@RequestMapping("/api/moves")
public class MoveController {

    private final MoveService moveService;

    public MoveController(MoveService moveService) {
        this.moveService = moveService;
    }

    @GetMapping
    public ResponseEntity<List<MoveResponse>> recupererTousLesMoves() {
        return ResponseEntity.ok(
            moveService.recupererTousLesMoves()
                .stream()
                .map(MoveResponse::from)
                .toList()
        );
    }

    @GetMapping("/{moveId}")
    public ResponseEntity<MoveResponse> recupererMoveParId(@PathVariable Long moveId) {
        return ResponseEntity.ok(MoveResponse.from(moveService.recupererMoveParId(moveId)));
    }

    @GetMapping("/type/{typeMove}")
    public ResponseEntity<List<MoveResponse>> recupererMovesParType(@PathVariable TypeMove typeMove) {
        return ResponseEntity.ok(
            moveService.recupererMovesParType(typeMove)
                .stream()
                .map(MoveResponse::from)
                .toList()
        );
    }
}