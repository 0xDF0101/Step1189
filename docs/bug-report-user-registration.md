# Bug Report: 일반 회원가입 오류 분석 및 수정 방향

## 1. Bug Report (버그 리포트)

### 근본 원인 분석

#### [버그 1] `User.java:80-87` — 잘못 설계된 생성자

```java
public User(UserCreateRequest request) {
    this.email = request.email();
    this.username = request.username();
    this.socialType = "Google";   // ← OAuth 전용 값 하드코딩
    this.providerId = "000000";   // ← OAuth 전용 값 하드코딩
    this.role = Role.USER;
    // password 필드가 아예 없음!
}
```

`UserCreateRequest`를 파라미터로 받는 이 생성자는 로컬 회원가입에 쓰일 것처럼 생겼지만, 실제로는 `socialType = "Google"`, `providerId = "000000"`으로 고정된 **OAuth 전용 동작**을 합니다. 더 심각한 문제는 **`password` 필드를 전혀 설정하지 않는다**는 점입니다. 이 생성자를 통해 만들어진 `User`는 비밀번호가 `null`인 상태로 DB에 저장됩니다.

`UserService.signUp()` (47번 줄)은 Builder를 사용하므로 이 생성자를 직접 호출하지 않습니다. 하지만 `UserService.createUser()` (32번 줄, "임시 로직")은 이 생성자를 직접 호출합니다. 만약 어느 경로에서라도 `signUp()` 대신 `createUser()`가 호출된다면, 비밀번호 없이 `socialType = "Google"`인 계정이 생성됩니다.

---

#### [버그 2] `UserController.java:27-31` + `OAuth2UserService.java:45-66` — OAuth2 흐름과의 충돌

```java
// UserController.java:27-30
@InitBinder
protected void initBinder(WebDataBinder binder) {
    binder.addValidators(validator); // UserEmailDuplicateValidator 전역 등록
}
```

`@InitBinder`에 대상 지정이 없으므로, 이 컨트롤러의 **모든 바인딩**에 `UserEmailDuplicateValidator`가 적용됩니다.

충돌 흐름:

1. 사용자가 Google OAuth2로 최초 로그인 → `OAuth2UserService.loadUser()` 실행
2. `OAuth2UserService.java:53-59`: DB에 `role = PRE_USER`, `email = <구글 이메일>`인 유저를 `saveAndFlush()`로 **즉시 저장**
3. 이후 동일한 이메일로 일반 회원가입 시도 → `UserController.createUser()` 실행
4. `@Valid` + `@InitBinder`로 `UserEmailDuplicateValidator`가 실행되어 해당 이메일이 이미 DB에 있음을 감지
5. `bindingResult.hasErrors() == true` → `UserController.java:51`: `EmailDuplicateException` throw
6. **회원가입 불가**

즉, OAuth2로 한 번이라도 로그인한 이메일은 이후 일반 가입이 영구적으로 차단됩니다.

---

#### [버그 3] `UserController.java:54` — 도달 불가능한 null 체크 (부가 버그)

```java
// UserController.java:54
if(request == null) {
    throw new IllegalArgumentException();
}
```

`@RequestBody`로 바인딩에 실패하면 Spring이 컨트롤러 진입 전에 `HttpMessageNotReadableException`을 throw합니다. 따라서 이 null 체크는 절대 실행되지 않는 dead code입니다.

---

#### [버그 4] `UserController.java:60` — 로그 포맷 오류 (부가 버그)

```java
log.info("회원 등록 완료 : ", request.username()); // {} 없음
```

SLF4J 포맷 플레이스홀더 `{}`가 없어서 `request.username()` 값이 로그에 출력되지 않습니다.

---

## 2. Fix Guidelines (수정 방향)

### [수정 1] `User.java` — 혼란을 야기하는 생성자 제거 또는 명확화

`User(UserCreateRequest request)` 생성자는 삭제하고, `UserService.createUser()` (임시 로직)도 함께 제거하는 것이 바람직합니다. 모든 유저 생성은 `UserService.signUp()` (Builder 방식)을 단일 진입점으로 사용해야 합니다.

만약 테스트나 내부 용도로 반드시 생성자 방식이 필요하다면, OAuth 전용 생성자와 Local 전용 생성자를 **명시적으로 분리**해야 합니다. 하나의 생성자에 OAuth 값을 하드코딩하고 `UserCreateRequest`를 파라미터로 받는 것은 의미론적 오류입니다.

---

### [수정 2] OAuth2 흐름과 일반 가입 흐름의 이메일 공간 분리

핵심 설계 원칙: **OAuth로 임시 저장된 `PRE_USER`와 일반 가입의 `USER`는 서로 다른 계정 유형임을 명확히 구분해야 합니다.**

`UserEmailDuplicateValidator`가 단순히 "이메일 존재 여부"만 체크하면, `PRE_USER`도 중복으로 처리됩니다. 수정 방향은 두 가지입니다:

**방법 A**: 검증기를 수정하여 동일 이메일이 있더라도 `role == PRE_USER`인 경우 중복으로 처리하지 않고, 대신 기존 `PRE_USER` 계정을 Local 계정으로 **전환(merge)** 하는 로직을 `signUp()` 안에서 처리합니다.

**방법 B**: OAuth 임시 유저를 DB에 바로 저장하지 않고, **세션(Session)이나 임시 저장소**에만 보관한 뒤 `setUsername()`에서 최종 정보를 입력받은 시점에 실제 DB에 저장합니다. 이렇게 하면 이메일 충돌 자체가 발생하지 않습니다.

---

### [수정 3] `@InitBinder` 적용 범위 제한

```java
// 수정 전: 컨트롤러 전체에 적용
@InitBinder
protected void initBinder(WebDataBinder binder) { ... }

// 수정 후: 특정 모델만 대상으로 지정
@InitBinder("userCreateRequest")
protected void initBinder(WebDataBinder binder) { ... }
```

`@InitBinder`에 대상 모델 이름을 명시하여, `setUsername` 엔드포인트 등 다른 바인딩에 검증기가 의도치 않게 적용되는 것을 방지해야 합니다.

---

### [수정 4] 아키텍처 원칙 요약

| 흐름 | 진입점 | 저장 시점 | `socialType` | `role` |
|---|---|---|---|---|
| 일반 회원가입 | `POST /api/v1/users` | 즉시 | `"Local"` | `USER` |
| OAuth2 최초 로그인 | `OAuth2UserService.loadUser()` | username 입력 후 | `"Google"` 등 | `PRE_USER` → `USER` |

이 두 흐름은 **이메일 공간을 공유하되, `role`과 `socialType`으로 구분**하거나, **OAuth 임시 상태를 DB 외부(세션)에서 관리**함으로써 충돌 없이 공존할 수 있습니다.
