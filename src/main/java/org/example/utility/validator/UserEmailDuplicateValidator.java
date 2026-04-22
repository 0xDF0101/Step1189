package org.example.utility.validator;


import lombok.RequiredArgsConstructor;
import org.example.dto.user.UserCreateRequest;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * 회원가입 시, 중복 이메일이 있는지 검증하는 validator
 *
 * ** 이제 필요가 없지만 아까우니까 남겨두겠음 **
 */

//@Component
@RequiredArgsConstructor
public class UserEmailDuplicateValidator implements Validator {

    private final UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return UserCreateRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserCreateRequest dto = (UserCreateRequest) target;
        if(userRepository.existsUserByEmail(dto.email())) {
            errors.rejectValue("email", "duplicate", "이미 존재하는 이메일입니다.");
        }
    }
}
