# BibleService / BibleController 코드 리뷰

> 작성일: 2026-05-06

---

## 1. BibleService.java

### 1-1. `cachedBibles` 필드 — null 안전성 없음 (버그 위험)

```java
private List<Bible> cachedBibles;

public List<Bible> getAllBibles() {
    return cachedBibles; // run() 완료 전 호출되면 null 반환
}
```

`ApplicationRunner.run()`이 완료되기 전에 `getAllBibles()`가 호출되면 `null`을 반환합니다.
서버 부팅 중 헬스체크나 빠른 요청이 들어오면 `NullPointerException`으로 이어질 수 있습니다.

**개선 방안**
```java
public List<Bible> getAllBibles() {
    if (cachedBibles == null) {
        throw new IllegalStateException("성경 데이터가 아직 초기화되지 않았습니다.");
    }
    return cachedBibles;
}
```
또는 초기값을 빈 리스트로 설정해두고, 아직 준비 중임을 알리는 방어 로직을 추가합니다.

---

### 1-2. `cachedBibles` 필드 — 스레드 가시성 문제

`cachedBibles`는 `volatile` 키워드 없이 선언되어 있습니다.
멀티스레드 환경에서 `run()`이 한 스레드에서 완료되더라도, 다른 스레드에서 변경이 보이지 않을 수 있습니다.

**개선 방안**
```java
private volatile List<Bible> cachedBibles;
```

---

### 1-3. `cachedBibles` 필드 — 가변 리스트 노출

`bibleRepository.findAll()`이 반환하는 리스트는 외부에서 수정할 수 있는 가변 리스트입니다.
만약 호출부에서 실수로 리스트를 변경하면 캐시가 오염됩니다.

**개선 방안**
```java
this.cachedBibles = List.copyOf(list); // 불변 리스트로 저장
```

---

### 1-4. `getBibleInfo()` — 캐시를 활용하지 않음 (불일치)

```java
public Bible getBibleInfo(int bibleId) {
    return bibleRepository.findById(bibleId) // 매번 DB 조회
            .orElseThrow(() -> new EntityNotFoundException("해당 성경을 찾을 수 없습니다."));
}
```

`getAllBibles()`는 캐시를 사용하는데, `getBibleInfo()`는 매번 DB를 조회합니다.
성경 데이터는 정적 데이터이므로 캐시에서 꺼내는 것이 일관성 있고 효율적입니다.

**개선 방안**
```java
public Bible getBibleInfo(int bibleId) {
    return cachedBibles.stream()
            .filter(b -> b.getId() == bibleId)
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("해당 성경을 찾을 수 없습니다."));
}
```
단, 이 경우 `cachedBibles` null 체크가 선행되어야 합니다.

---

### 1-5. `ApplicationRunner` vs `@PostConstruct`

`ApplicationRunner`는 Spring Application Context가 완전히 준비된 뒤에 실행되므로 `data.sql`이 로딩된 후임을 보장한다는 장점이 있습니다. 이 점은 올바른 선택입니다.

다만 `@PostConstruct`와 달리 테스트 환경에서 직접 제어하기 어려울 수 있으므로, 통합 테스트 작성 시 이 점을 유의해야 합니다.

---

## 2. BibleController.java

### 2-1. `@ModelAttribute`와 `model.addAttribute()` 중복 호출 (성능 낭비)

```java
@ModelAttribute("bibles")
public List<Bible> bibles() {
    return bibleService.getAllBibles(); // 모든 요청에서 자동 실행
}

@GetMapping("/bibles")
public String getBibles(@LoginUser Long userId, Model model) {
    model.addAttribute("bibles", bibleService.getAllBibles()); // 또 한번 실행
    model.addAttribute("userProgress", jpaProgressServiceImpl.getAllProgress(userId));
    return "bible-list";
}
```

`@ModelAttribute` 메서드는 이 컨트롤러의 모든 요청 처리 전에 자동으로 실행됩니다.
`/bibles` GET 요청이 오면 `getAllBibles()`가 두 번 호출됩니다.

**개선 방안**: `getBibles()`에서 중복 addAttribute를 제거합니다.
```java
@GetMapping("/bibles")
public String getBibles(@LoginUser Long userId, Model model) {
    model.addAttribute("userProgress", jpaProgressServiceImpl.getAllProgress(userId));
    return "bible-list";
}
```

---

### 2-2. 구체 구현체에 직접 의존 (DIP 위반)

```java
private final JpaProgressServiceImpl jpaProgressServiceImpl; // 구체 클래스
```

인터페이스인 `ProgressService`가 있음에도 구체 구현체를 직접 주입받고 있습니다.
구현체가 바뀌거나 테스트 시 Mock 주입이 어려워집니다.

**개선 방안**
```java
private final ProgressService progressService; // 인터페이스 사용
```

---

### 2-3. `/bibles/{bibleId}` — 예외 처리 미완성 (TODO 방치)

```java
if(bibleId < 1 || bibleId > 66) {
    // TODO 예외처리 똑바로 하기
    throw new IllegalArgumentException();
}
```

`IllegalArgumentException`은 `ApiExceptionAdvice`에서 핸들링하지 않아 500 오류로 응답됩니다.
메시지도 없어 디버깅이 어렵습니다.

**개선 방안**: 커스텀 예외나 `@ExceptionHandler`를 추가하거나, Bean Validation으로 대체합니다.
```java
// 예시: 메시지라도 추가
throw new IllegalArgumentException("bibleId는 1~66 사이여야 합니다: " + bibleId);
// 그리고 ApiExceptionAdvice에 IllegalArgumentException 핸들러 추가
```

---

### 2-4. 매직 넘버 `66`

```java
if(bibleId < 1 || bibleId > 66)
```

`66`은 성경 권수를 의미하는 도메인 상수인데 하드코딩되어 있습니다.
나중에 이 숫자가 여러 곳에 흩어지면 관리가 어려워집니다.

**개선 방안**
```java
private static final int BIBLE_MAX_ID = 66;
private static final int BIBLE_MIN_ID = 1;
```
또는 캐싱된 `cachedBibles.size()`를 활용해 동적으로 상한을 결정합니다.

---

### 2-5. API URL 경로 일관성 문제

프로젝트의 다른 API는 `/api/v1/` 접두사를 사용하는데, `/bibles/{bibleId}`와 `/bibles`는 그렇지 않습니다.

| 엔드포인트 | 현재 경로 | 권장 경로 |
|---|---|---|
| 성경 단건 조회 (API) | `GET /bibles/{bibleId}` | `GET /api/v1/bibles/{bibleId}` |
| 성경 목록 페이지 (View) | `GET /bibles` | 현행 유지 (View는 무방) |

API 응답(`@ResponseBody`)과 View 렌더링 엔드포인트가 같은 컨트롤러에 섞여있는 것도 혼란을 줄 수 있습니다. 분리를 고려할 수 있습니다.

---

## 3. 요약

| 분류 | 항목 | 심각도 |
|---|---|---|
| 버그 위험 | `cachedBibles` null 반환 가능 | 높음 |
| 버그 위험 | `volatile` 미적용으로 스레드 가시성 문제 | 중간 |
| 성능 | `getAllBibles()` 이중 호출 | 낮음 |
| 성능 | `getBibleInfo()`가 캐시 미활용 | 낮음 |
| 설계 | 구체 구현체 직접 의존 (DIP 위반) | 중간 |
| 설계 | 가변 리스트 캐시 노출 | 중간 |
| 유지보수 | 매직 넘버 `66` 하드코딩 | 낮음 |
| 유지보수 | TODO 방치 (`예외처리 똑바로 하기`) | 중간 |
| 일관성 | API URL에 `/api/v1/` 접두사 누락 | 낮음 |
