package org.example.user.dto;

public record UserCreateRequest(

        String nickname,
        String email,
        String statusMessage

) {
}
