package org.example.service;

import org.example.dto.plan.PlanRequest;
import org.example.dto.plan.PlanStatsResponse;

import java.util.Optional;

public interface PlanService {
    void savePlan(Long userId, PlanRequest request);
    void deletePlan(Long userId);
    Optional<PlanStatsResponse> getStats(Long userId);
}
