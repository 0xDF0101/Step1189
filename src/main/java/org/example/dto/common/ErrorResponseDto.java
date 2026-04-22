package org.example.dto.common;

public record ErrorResponseDto (
        String field, // 에러가 발생한 필드명
        String message // 에러 메시지
){
}

