package org.example.security;

import org.example.config.SecurityConfig;
import org.example.controller.api.OAuth2SuccessHandler;
import org.example.controller.api.ProgressController;
import org.example.service.ProgressService;
import org.example.service.auth.CustomUserDetailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProgressController.class)
@Import(SecurityConfig.class)
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProgressService progressService;

    @MockBean
    private CustomUserDetailService customUserDetailService;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/progress", "/api/v1/main"})
    @DisplayName("로그인하지 않은 사용자가 보호된 API(GET)에 접근 시 리다이렉트")
    void unauthenticated_get_access_test(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 Progress에 Post 요청 시 거부")
    void unauthenticated_user_access_test2() throws Exception {
        mockMvc.perform(post("/api/v1/progress"))
                .andExpect(status().is3xxRedirection());
    }

}
