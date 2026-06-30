package org.example.repository;

import org.example.entity.ReadingPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReadingPlanRepository extends JpaRepository<ReadingPlan, Long> {
    Optional<ReadingPlan> findByUserId(Long userId);
}
