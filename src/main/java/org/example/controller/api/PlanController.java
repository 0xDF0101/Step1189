package org.example.controller.api;

import lombok.RequiredArgsConstructor;
import org.example.dto.plan.PlanRequest;
import org.example.dto.plan.PlanStatsResponse;
import org.example.service.PlanService;
import org.example.utility.annotation.LoginUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping("/plan")
    public ResponseEntity<PlanStatsResponse> getPlan(@LoginUser Long userId) {
        return planService.getStats(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/plan")
    public ResponseEntity<Void> savePlan(
            @LoginUser Long userId,
            @RequestBody PlanRequest request
    ) {
        planService.savePlan(userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/plan")
    public ResponseEntity<Void> deletePlan(@LoginUser Long userId) {
        planService.deletePlan(userId);
        return ResponseEntity.ok().build();
    }
}
