package org.example.service.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.entity.User;
import org.example.model.Role;
import org.example.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * 외부 OAuth2 제공자(Google 등)에 실제 HTTP 요청을 보내 사용자 정보를 가져옵니다.
     *
     * <p>단위 테스트에서 실제 네트워크 호출 없이 {@code super.loadUser()}를 스텁할 수 있도록
     * protected 메서드로 분리했습니다. 프로덕션 코드에서는 직접 호출하지 마세요.</p>
     *
     * @param userRequest OAuth2 인증 요청 정보 (액세스 토큰, 클라이언트 등록 정보 포함)
     * @return 외부 제공자로부터 받은 {@link OAuth2User} 정보
     */
    protected OAuth2User fetchFromProvider(OAuth2UserRequest userRequest) {
        return super.loadUser(userRequest);
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 구글로부터 유저 정보를 가져옴
        OAuth2User oAuth2User = fetchFromProvider(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String name = (String) attributes.get("name");

        String username = "TEMP_" + UUID.randomUUID().toString().substring(0, 8) + name;
        // --> 추후에 직접 username을 정할 수 있도록 하고, 우선은 그냥 임시로 채워넣는다.
        String email = (String) attributes.get("email");
        // 💡 1. 제공자 정보 가져오기 (google)
        String provider = userRequest.getClientRegistration().getRegistrationId();
        // 💡 2. 제공자 고유 ID 가져오기 (구글은 "sub"이라는 키를 사용해)
        String providerId = (String) attributes.get("sub");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    return User.builder()
                            .username(username)
                            .email(email)
                            .socialType(provider)
                            .providerId(providerId)
                            .role(Role.PRE_USER)
                            .build();
                });
        /**
         * 실제 Entity에서는 provider가 아닌 SocialType으로 저장됨
         */

        userRepository.saveAndFlush(user); // 쓰기 지연이 일어나버리면 핸들러에서 캐치를 못함

        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }
}
