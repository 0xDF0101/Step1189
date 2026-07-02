package org.example.dto.group;

import java.time.LocalDate;
import java.util.List;

public record GroupGoalDto(
        LocalDate startDate,
        LocalDate endDate,
        int targetChaptersPerPerson,
        List<MemberGoalProgressDto> memberProgress
) {}
