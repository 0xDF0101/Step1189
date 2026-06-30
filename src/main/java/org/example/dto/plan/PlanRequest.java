package org.example.dto.plan;

import java.time.LocalDate;

public record PlanRequest(LocalDate startDate, LocalDate endDate) {
}
