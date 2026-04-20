package org.example.service;

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

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 구글로부터 유저 정보를 가져옴
        OAuth2User oAuth2User = super.loadUser(userRequest);

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
//                .map(entity -> {
//                    entity.updateName(name); // 이름 업데이트 메서드가 있다면 활용
//                    return entity;
//                })
                .orElseGet(() -> {
                    // 신규 유저 생성 (생성자 파라미터 순서는 유진이 코드에 맞춰야 해!)
//                    return new User(username, email, provider, providerId);
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

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey())),
                attributes,
                "email"
        );
    }
}
