package org.example.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.progress.RecordRequest;
import org.example.dto.user.CustomUserDetails;
import org.example.service.ProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.PathMatcher;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 테스트 항목
 * 1. GET /progress 시 올바르게 return
 * 2. POST /progress 시 올바르게 return ..?
 * 3. GET /main 시 올바르게 return
 * 4. 비로그인 접근
 * 5. 잘못된 세션
 * 6. 데이터 유효성
 * 7. 엣지 케이스
 */


@WebMvcTest(ProgressController.class)
class ProgressControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProgressService progressService;


    Map<Integer, Map<Integer, Integer>> progress;

    CustomUserDetails mockUser;
    @Autowired
    private PathMatcher mvcPathMatcher;

    @BeforeEach
    void set() {
        progress = Map.of(
                1, Map.of(1, 10, 2, 5),  // 1권: 1장 10회, 2장 5회
                2, Map.of(1, 3)          // 2권: 1장 3회
        );
        mockUser = mock(CustomUserDetails.class);
    }

    @Test
    @DisplayName("모든 장의 진척도 요청 성공")
    void get_progress_success() throws Exception {
        when(mockUser.getUserId()).thenReturn(1L);
        when(progressService.getAllProgress(1L)).thenReturn(progress);

        mockMvc.perform(get("/api/v1/progress")
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.['1']").isNotEmpty());

        verify(progressService).getAllProgress(1L);
    }

    @Test
    @DisplayName("로그인되지 않은 사용자")
    void no_login_access() throws Exception {
        when(progressService.getAllProgress(1L)).thenReturn(progress);

        mockMvc.perform(get("/api/v1/progress"))
                .andExpect(status().is3xxRedirection());

    }

    @Test
    @DisplayName("특정 장을 읽음 표시 성공")
    void recordProgress_success() throws Exception {
        when(mockUser.getUserId()).thenReturn(1L);
        RecordRequest request = new RecordRequest(1, 1);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/progress")
                .with(user(mockUser))
                .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(progressService).recordProgress(1L, request);
    }

    @Test
    @DisplayName("하루에 읽은 양을 가져옴")
    void dailyProgress() throws Exception {
        when(mockUser.getUserId()).thenReturn(1L);
        Map<LocalDate, Integer> result = Map.of(LocalDate.now(),1);
        when(progressService.getDailyProgress(1L)).thenReturn(result);

        mockMvc.perform(get("/api/v1/main")
                .with(user(mockUser)))
                .andExpect(status().isOk());

        verify(progressService).getDailyProgress(1L);

    }
}



























