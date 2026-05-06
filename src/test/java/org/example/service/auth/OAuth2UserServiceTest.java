package org.example.service.auth;

import org.example.entity.User;
import org.example.model.Role;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * {@link OAuth2UserService}의 단위 테스트 클래스.
 *
 * <p>Google OAuth2 로그인 흐름에서 핵심 비즈니스 로직을 검증합니다:</p>
 * <ul>
 *   <li>신규 유저: DB에 {@link Role#PRE_USER} 권한으로 저장</li>
 *   <li>기존 유저: 기존 엔티티 그대로 saveAndFlush 재호출 (역할 변경 없음)</li>
 *   <li>반환 타입: 항상 {@link CustomUserDetails} (UserDetails + OAuth2User 통합 구현체)</li>
 *   <li>OAuth2 속성: 구글이 제공한 attributes가 CustomUserDetails에 그대로 전달</li>
 * </ul>
 *
 * <h2>테스트 전략 — fetchFromProvider 분리</h2>
 * <p>
 * {@link org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService#loadUser}는
 * 실제 HTTP 요청을 발생시키므로 단위 테스트에서 직접 호출할 수 없습니다.
 * 기존 테스트에서 {@code spy}로 {@code loadUser} 전체를 스텁하면
 * 비즈니스 로직 자체가 실행되지 않아 아무것도 검증되지 않는 문제가 있었습니다.
 * </p>
 * <p>
 * 이를 해결하기 위해 {@link OAuth2UserService}에
 * {@code protected fetchFromProvider()} 메서드를 추출하고,
 * Mockito {@code spy}로 해당 메서드만 스텁하여 나머지 비즈니스 로직을 격리 테스트합니다.
 * </p>
 *
 * <pre>
 * [실제 흐름]          [테스트 흐름]
 * loadUser()           loadUser()          ← 실제 실행
 *   └─ super.loadUser()  └─ fetchFromProvider()  ← spy로 스텁 (HTTP 차단)
 *        └─ HTTP 호출           └─ mockOAuth2User 반환
 * </pre>
 */
@ExtendWith(MockitoExtension.class)
class OAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OAuth2UserService oAuth2UserService;

    private OAuth2UserRequest mockRequest;
    private OAuth2User mockOAuth2User;

    /**
     * 각 테스트 전에 공통으로 사용할 가짜 OAuth2 데이터를 초기화합니다.
     *
     * <ul>
     *   <li>{@code mockRequest}: 실제 Google 클라이언트 등록 구조를 모방한 요청 객체</li>
     *   <li>{@code mockOAuth2User}: 구글이 반환하는 사용자 정보 (sub·name·email 포함)</li>
     * </ul>
     */
    @BeforeEach
    void setUp() {
        mockRequest = createMockRequest();

        Map<String, Object> attributes = Map.of(
                "sub", "google_12345",
                "name", "김유진",
                "email", "eugene@example.com"
        );
        mockOAuth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
        );
    }

    /**
     * 신규 구글 유저가 처음 로그인하는 경우를 검증합니다.
     *
     * <h3>시나리오</h3>
     * <ol>
     *   <li>구글이 {@code sub, name, email} 속성을 포함한 OAuth2 사용자 정보를 반환한다.</li>
     *   <li>DB에 해당 이메일의 유저가 존재하지 않는다 ({@link Optional#empty()}).</li>
     *   <li>서비스가 {@link Role#PRE_USER} 권한으로 신규 유저를 생성하여 DB에 저장한다.</li>
     *   <li>반환된 객체는 {@link CustomUserDetails}이며, email·role·socialType·providerId가 올바르다.</li>
     * </ol>
     *
     * <h3>PRE_USER 역할의 의미</h3>
     * <p>
     * Google OAuth2 최초 로그인 후 username을 아직 설정하지 않은 준회원 상태입니다.
     * {@code OAuth2SuccessHandler}는 이 역할을 감지해 {@code /signup/set-username}으로 리다이렉트합니다.
     * </p>
     */
    @Test
    @DisplayName("신규 구글 유저가 로그인하면 PRE_USER 권한으로 DB에 저장된다")
    void loadUser_newUser_savedWithPreUserRole() {
        // Given
        OAuth2UserService spyService = spy(oAuth2UserService);
        doReturn(mockOAuth2User).when(spyService).fetchFromProvider(mockRequest);

        when(userRepository.findByEmail("eugene@example.com")).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        OAuth2User result = spyService.loadUser(mockRequest);

        // Then
        assertThat(result).isInstanceOf(CustomUserDetails.class);
        CustomUserDetails details = (CustomUserDetails) result;

        assertThat(details.getUser().getEmail()).isEqualTo("eugene@example.com");
        assertThat(details.getUser().getRole()).isEqualTo(Role.PRE_USER);
        assertThat(details.getUser().getSocialType()).isEqualTo("google");
        assertThat(details.getUser().getProviderId()).isEqualTo("google_12345");
        verify(userRepository, times(1)).saveAndFlush(any(User.class));
    }

    /**
     * 이미 가입된 구글 유저가 재로그인하는 경우를 검증합니다.
     *
     * <h3>시나리오</h3>
     * <ol>
     *   <li>구글이 OAuth2 사용자 정보를 반환한다.</li>
     *   <li>DB에 해당 이메일의 유저가 이미 존재한다.</li>
     *   <li>서비스는 신규 User 객체를 생성하지 않고 기존 유저를 그대로 saveAndFlush한다.</li>
     *   <li>기존 유저의 role(USER)과 username이 변경되지 않는다.</li>
     * </ol>
     *
     * <h3>기존 유저도 saveAndFlush하는 이유</h3>
     * <p>
     * 쓰기 지연(write-behind)을 방지하고, 이후 {@code OAuth2SuccessHandler}에서
     * 최신 엔티티 상태를 즉시 읽을 수 있도록 기존 유저도 flush합니다.
     * </p>
     */
    @Test
    @DisplayName("이미 가입된 구글 유저가 재로그인하면 기존 유저 정보로 saveAndFlush가 호출된다")
    void loadUser_existingUser_saveAndFlushCalledWithExistingUser() {
        // Given
        User existingUser = User.builder()
                .username("기존닉네임")
                .email("eugene@example.com")
                .socialType("google")
                .providerId("google_12345")
                .role(Role.USER)
                .build();

        OAuth2UserService spyService = spy(oAuth2UserService);
        doReturn(mockOAuth2User).when(spyService).fetchFromProvider(mockRequest);

        when(userRepository.findByEmail("eugene@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.saveAndFlush(existingUser)).thenReturn(existingUser);

        // When
        OAuth2User result = spyService.loadUser(mockRequest);

        // Then
        assertThat(result).isInstanceOf(CustomUserDetails.class);
        CustomUserDetails details = (CustomUserDetails) result;

        // 기존 유저 정보가 변경되지 않았는지 확인
        assertThat(details.getUser().getRole()).isEqualTo(Role.USER);
        assertThat(details.getUser().getUsername()).isEqualTo("기존닉네임");

        // 기존 유저 엔티티로 saveAndFlush가 호출됐는지 확인
        verify(userRepository, times(1)).saveAndFlush(existingUser);
    }

    /**
     * 로그인 결과로 반환되는 객체가 항상 {@link CustomUserDetails} 타입임을 검증합니다.
     *
     * <h3>의미</h3>
     * <p>
     * 이 프로젝트는 로컬 로그인({@link UserDetails})과 OAuth2 로그인을
     * {@link CustomUserDetails} 단일 구현체로 통합합니다.
     * Spring Security가 {@code Authentication} 객체 내부에 저장하는 principal이
     * 항상 {@code CustomUserDetails}여야 컨트롤러에서
     * {@code @AuthenticationPrincipal CustomUserDetails} 주입이 정상 동작합니다.
     * </p>
     */
    @Test
    @DisplayName("loadUser의 반환 타입은 UserDetails와 OAuth2User를 모두 구현한 CustomUserDetails이다")
    void loadUser_alwaysReturnsCustomUserDetails() {
        // Given
        OAuth2UserService spyService = spy(oAuth2UserService);
        doReturn(mockOAuth2User).when(spyService).fetchFromProvider(mockRequest);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        OAuth2User result = spyService.loadUser(mockRequest);

        // Then — CustomUserDetails는 UserDetails + OAuth2User 두 인터페이스를 모두 구현해야 한다
        assertThat(result).isInstanceOf(CustomUserDetails.class);
        assertThat(result).isInstanceOf(UserDetails.class);
        assertThat(result).isInstanceOf(OAuth2User.class);
    }

    /**
     * 구글로부터 받은 OAuth2 속성(attributes)이 {@link CustomUserDetails}에 그대로 전달되는지 검증합니다.
     *
     * <h3>의미</h3>
     * <p>
     * {@code CustomUserDetails.getAttributes()}는 구글이 제공한 원본 속성 맵을 그대로 보관해야 합니다.
     * 이 값은 {@code OAuth2SuccessHandler}나 컨트롤러에서
     * {@code principal.getAttributes().get("name")} 형태로 참조될 수 있습니다.
     * </p>
     */
    @Test
    @DisplayName("구글 OAuth2 attributes(sub·name·email)가 CustomUserDetails에 그대로 전달된다")
    void loadUser_oauth2AttributesPassedToCustomUserDetails() {
        // Given
        OAuth2UserService spyService = spy(oAuth2UserService);
        doReturn(mockOAuth2User).when(spyService).fetchFromProvider(mockRequest);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        OAuth2User result = spyService.loadUser(mockRequest);

        // Then
        CustomUserDetails details = (CustomUserDetails) result;
        assertThat(details.getAttributes())
                .containsEntry("sub", "google_12345")
                .containsEntry("name", "김유진")
                .containsEntry("email", "eugene@example.com");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 헬퍼 메서드
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 테스트용 {@link OAuth2UserRequest}를 생성하는 헬퍼 메서드.
     *
     * <p>
     * 실제 네트워크 호출 없이 {@link ClientRegistration}을 직접 빌드합니다.
     * {@code registrationId("google")}과 {@code userNameAttributeName("sub")}은
     * Google OAuth2 스펙을 그대로 모방한 값입니다.
     * </p>
     *
     * @return 테스트에서 사용할 가짜 {@link OAuth2UserRequest}
     */
    private OAuth2UserRequest createMockRequest() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("test-id")
                .clientSecret("test-secret")
                .authorizationUri("https://example.com/auth")
                .tokenUri("https://example.com/token")
                .userInfoUri("https://example.com/userinfo")
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .userNameAttributeName("sub")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();

        return new OAuth2UserRequest(
                clientRegistration,
                new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "fake-token", null, null)
        );
    }
}
