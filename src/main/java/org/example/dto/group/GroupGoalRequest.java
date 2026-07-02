package org.example.dto.group;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GroupGoalRequest(
        @NotNull(message = "시작일을 입력해주세요")
        LocalDate startDate,

        @NotNull(message = "종료일을 입력해주세요")
        LocalDate endDate,

        @Min(value = 1, message = "목표 장 수는 1장 이상이어야 합니다")
        int targetChaptersPerPerson
) {}
