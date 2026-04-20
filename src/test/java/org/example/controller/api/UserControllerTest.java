package org.example.controller.api;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.EmailDuplicateException;
import org.example.service.UserService;
import org.example.validator.UserEmailDuplicateValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Slf4j
@WebMvcTest(value = UserController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class}
)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private UserEmailDuplicateValidator validator;

    String json;
    @BeforeEach
    void set() {
        json = """
                 {
                     "email": "asdf@email.com",
                     "username": "eugene",
                     "password": "qwer1234"
                 }
                """;
    }

    @Test
    @DisplayName("회원가입시 signup()이 제대로 호출됨")
    void signUp_success() throws Exception {
        when(validator.supports(any())).thenReturn(true);
        doNothing().when(userService).signUp(any());




        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)) // JSON 응답을 기대
                .andExpect(status().isOk());

        verify(userService, times(1)).signUp(any());

    }

    @Test
    @DisplayName("이메일이 중복되었을 때, 예외 발생")
    void duplicate_email_exception() throws Exception {
        when(validator.supports(any())).thenReturn(true);


        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("email", "duplicate", "이미 존재하는 이메일입니다.");
            return null;
        }).when(validator).validate(any(), any());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(result ->
                            Assertions.assertTrue(
                                    result.getResolvedException() instanceof EmailDuplicateException
                            ))
                        .andExpect(status().isConflict());




    }



}