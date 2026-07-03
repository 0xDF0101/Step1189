package org.example.controller.api;

import lombok.RequiredArgsConstructor;
import org.example.dto.poke.PokeResponse;
import org.example.service.PokeService;
import org.example.utility.annotation.LoginUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pokes")
public class PokeController {

    private final PokeService pokeService;

    @PostMapping("/{targetUserId}")
    public ResponseEntity<Void> poke(@LoginUser Long userId, @PathVariable Long targetUserId) {
        pokeService.poke(userId, targetUserId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<PokeResponse>> getReceivedPokes(@LoginUser Long userId) {
        return ResponseEntity.ok(pokeService.getReceivedPokes(userId));
    }
}
