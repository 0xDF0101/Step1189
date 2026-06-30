package org.example.dto.plan;

import java.time.LocalDate;

public record PlanStatsResponse(
        LocalDate startDate,
        LocalDate endDate,
        int totalDays,
        int daysElapsed,
        int daysRemaining,
        int targetPerDay,
        int chaptersReadInPeriod,
        int expectedByNow,
        boolean onTrack,
        double percentComplete,
        int adjustedDailyTarget
) {
}
