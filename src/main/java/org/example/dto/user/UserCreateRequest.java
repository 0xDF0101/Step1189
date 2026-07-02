package org.example.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @Email(message = "이메일 형식이 올바르지 않습니다")
        @NotBlank(message = "이메일을 입력해주세요")
        String email,
        @NotBlank(message = "아이디를 입력해주세요")
        @Size(min = 4, max = 20, message = "아이디는 4~20자여야 합니다")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "영문, 숫자, -, _만 사용할 수 있습니다")
        String username,
        @NotBlank(message = "표시 이름을 입력해주세요")
        @Size(min = 2, max = 20, message = "표시 이름은 2~20자여야 합니다")
        String displayName,
        @NotBlank(message = "비밀번호를 입력해주세요")
        @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다")
        String password
) {
}
