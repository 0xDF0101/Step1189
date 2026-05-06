# 인증 로직 코드리뷰 (CustomUserDetails 통합 방식)

**검토 대상 파일**
- `service/auth/CustomUserDetails.java`
- `service/auth/CustomUserDetailService.java`
- `service/auth/OAuth2UserService.java`
- `config/SecurityConfig.java`
- `controller/api/OAuth2SuccessHandler.java`
- `controller/api/UserController.java` (인증 관련 부분)

---

## 결론 요약

| 심각도 | 건수 |
|--------|------|
| 🔴 Critical | 1 |
| 🟡 Medium   | 4 |
| 🟢 Low      | 3 |

---

## 🔴 Critical

### 1. `getUsername()`이 email이 아닌 username을 반환 — 로그인 식별자 불일치

**파일:** `CustomUserDetails.java:54`

```java
@Override
public String getUsername() {
    return user.getUsername(); // DB의 username 컬럼 반환
}
```

**문제:**  
`SecurityConfig`에서 form login의 식별자를 `email`로 설정했다.

```java
.usernameParameter("email")
```

Spring Security의 `DaoAuthenticationProvider`는 form에서 받은 `email`을 `loadUserByUsername(email)`에 전달하고, 인증 성공 후 `Authentication.getName()`의 값을 `UserDetails.getUsername()`으로 채운다.

그 결과:
- `loadUserByUsername("hong@example.com")` — email로 조회 ✓
- `Authentication.getName()` → `"hong_gildong"` (username 필드 값)

이후 Spring Security가 사용자를 재로드해야 할 때(Remember-Me, 세션 재검증 등) `loadUserByUsername(authentication.getName())`을 호출하면 `"hong_gildong"`을 email로 조회하게 되어 `UsernameNotFoundException`이 발생한다.

현재 코드에서 Remember-Me나 특별한 세션 재검증이 없어 런타임 오류로 나타나지 않지만, 기능이 추가되는 순간 즉시 버그가 된다.

**수정 방법:**

```java
@Override
public String getUsername() {
    return user.getEmail(); // 로그인 식별자와 일치
}
```

---

## 🟡 Medium

### 2. form 로그인 사용자의 `getAttributes()` → null 반환 가능

**파일:** `CustomUserDetails.java:20`

```java
private Map<String, Object> attribute; // OAuth2 용
```

단일 인자 생성자로 생성된 form 로그인 사용자는 `attribute`가 `null`이다. 이 상태에서 코드 어딘가에서 `getAttributes()`를 호출하면 `null`이 반환되어 `NullPointerException`이 발생할 수 있다.

또한 `attribute` 필드가 `final`이 아니어서 외부에서 상태 변경이 가능하다(Principal 객체는 불변이어야 한다).

**수정 방법:**

```java
private final Map<String, Object> attribute;

// 단일 인자 생성자
public CustomUserDetails(User user) {
    this.user = user;
    this.attribute = Map.of(); // null 대신 빈 맵
}
```

---

### 3. `OAuth2SuccessHandler`에서 불필요한 DB 재조회

**파일:** `OAuth2SuccessHandler.java:35-48`

```java
OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
String email = (String) oAuth2User.getAttributes().get("email");

User user = userRepository.findByEmail(email)  // ← 불필요한 DB 쿼리
        .orElseThrow(...);

if (user.getRole() == Role.PRE_USER) { ... }
```

`authentication.getPrincipal()`은 이미 `OAuth2UserService.loadUser`가 반환한 `CustomUserDetails` 객체다. 해당 객체 안에 `User` 엔티티(role 포함)가 이미 존재한다. `UserRepository`를 주입받을 필요도 없고 DB를 다시 조회할 필요도 없다.

**수정 방법:**

```java
// UserRepository 의존성 제거 가능
CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
String targetUrl = principal.getUser().getRole() == Role.PRE_USER
        ? "/signup/set-username"
        : "/main";
getRedirectStrategy().sendRedirect(request, response, targetUrl);
```

---

### 4. `UserController.setUsername`에서 form 로그인 사용자 접근 시 NPE

**파일:** `UserController.java:56`

```java
@AuthenticationPrincipal OAuth2User oAuth2User
```

`CustomUserDetails implements OAuth2User`이므로 form 로그인 사용자도 이 파라미터에 바인딩된다. 이 경우 `attribute`가 `null`이어서 `oAuth2User.getAttribute("email")`을 호출하면 NPE가 발생한다.

현재는 이 엔드포인트를 OAuth2 흐름에서만 호출하도록 설계되어 있어 실제 발생 가능성은 낮지만, 잘못된 요청이 들어오면 500 에러가 발생한다.

**수정 방법:**  
파라미터 타입을 `CustomUserDetails`로 변경하거나, 이슈 2번의 수정(빈 맵 초기화)으로 NPE만큼은 방어할 수 있다.

```java
@AuthenticationPrincipal CustomUserDetails principal
String email = principal.getUser().getEmail();
```

---

### 5. `OAuth2UserService` — 임시 username 길이 오버플로우 가능성

**파일:** `OAuth2UserService.java:36`

```java
String username = "TEMP_" + UUID.randomUUID().toString().substring(0, 8) + name;
```

`User.username` 컬럼의 `length = 50`이다. Google `name` 속성이 37자를 초과하면 `"TEMP_"(5) + UUID(8) + name(37+)`가 50자를 넘어 DB 저장 시 오류가 발생한다. 실제 가능성은 낮지만 방어 코드가 없다.

**수정 방법:**

```java
String raw = "TEMP_" + UUID.randomUUID().toString().substring(0, 8) + name;
String username = raw.length() > 50 ? raw.substring(0, 50) : raw;
```

---

## 🟢 Low

### 6. `SecurityConfig.filterChain`의 미사용 파라미터

**파일:** `SecurityConfig.java:24`

```java
public SecurityFilterChain filterChain(HttpSecurity http,
        OAuth2ClientProperties oAuth2ClientProperties,  // ← 사용 안 함
        OAuth2SuccessHandler oAuth2SuccessHandler) throws Exception {
```

`oAuth2ClientProperties`는 주입받지만 메서드 내에서 전혀 사용되지 않는다. 제거가 필요하다.

또한 `oAuth2SuccessHandler`는 클래스 필드(`@RequiredArgsConstructor`로 주입)와 메서드 파라미터 양쪽에 중복 선언되어 있다. 필드 또는 파라미터 중 하나만 사용하도록 정리가 필요하다.

---

### 7. CSRF 비활성화

**파일:** `SecurityConfig.java:27`

```java
.csrf(csrf -> csrf.disable()) // 일단 꺼놓음
```

Form 로그인 + 세션 기반 인증에서 CSRF 보호는 필수다. 현재 `// 일단 꺼놓음` 주석이 있으나, 배포 전 반드시 활성화하거나 적절한 예외 경로만 허용해야 한다.

---

### 8. `@Transactional` import 불일치

**파일:** `OAuth2UserService.java:3`

```java
import jakarta.transaction.Transactional;
```

Spring Boot에서 `jakarta.transaction.Transactional`도 동작하지만, Spring AOP 트랜잭션 관리와의 통합은 `org.springframework.transaction.annotation.Transactional`을 사용해야 더 명확하다. Spring의 `@Transactional`은 `readOnly`, `propagation`, `isolation` 등 세밀한 제어가 가능하다.

---

## CustomUserDetails 통합 방식 종합 평가

`UserDetails`와 `OAuth2User`를 하나의 클래스로 통합하는 패턴 자체는 올바른 접근이다. Principal 객체를 통일하면 `@AuthenticationPrincipal`로 컨트롤러에서 일관되게 사용자 정보를 꺼낼 수 있다는 장점이 있다.

다만 현재 구현에서 **Critical 이슈인 `getUsername()`의 반환값 불일치**가 이 통합의 핵심 결함이다. `UserDetails.getUsername()`의 계약(contract)은 "이 사용자를 `loadUserByUsername`으로 다시 로드할 수 있는 값을 반환하라"이며, 현재는 이 계약을 위반하고 있다.

나머지 Medium 이슈들은 통합 방식 자체보다는 null 처리나 레이어 역할 혼재에서 비롯된 문제들이다.
