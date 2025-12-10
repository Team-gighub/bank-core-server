# 프로젝트 코드 스타일 가이드 (Gemini Code Assist)

이 문서는 본 프로젝트의 코드 리뷰 기준 및 스타일 가이드를 정의합니다.  
Gemini Code Assist는 아래 기준에 따라 코드를 자동 점검합니다.

---

# 1. 일반 원칙 (General Principles)

## 1.1 SOLID 원칙 준수
- **SRP**: 클래스/메서드는 하나의 책임만 가진다.
- **OCP**: 기능 확장은 허용하되, 기존 코드 수정은 최소화한다.
- **LSP**: 자식 클래스는 부모 클래스와 호환되어야 한다.
- **ISP**: 클라이언트는 사용하지 않는 메서드에 의존하지 않는다.
- **DIP**: 고수준 모듈은 구체 구현이 아닌 추상화에 의존한다.

## 1.2 클린 코드 (Clean Code)
- **네이밍 규칙**
    - 클래스: 명사(Noun)
    - 메서드: 동사(Verb)
    - 변수: 목적을 분명히 나타내는 명확한 의도 기반 이름 사용
- 함수는 하나의 기능만 수행하도록 유지한다.
- 매직 넘버/문자열을 상수 또는 enum으로 치환한다.
- **불변성(Immutability)**
    - 가능한 경우 `final` 사용
    - 불필요한 setter 생성 금지
- 주석은 *무엇*보다는 **왜(WHY)** 를 설명한다.

---

# 2. 백엔드 개발 가이드라인 (Spring Boot)

## 2.1 아키텍처 구조
- 계층구조를 따른다: **Controller → Service → Repository**.
- Controller는 요청 검증, 요청/응답 처리만 담당한다.
- Service는 비즈니스 로직을 캡슐화한다.
- Repository는 데이터 접근만 담당한다.

## 2.2 DI (Dependency Injection)
- **생성자 주입을 기본 원칙으로 한다.**
- 필드 주입(@Autowired)을 사용하지 않는다. (순환참조 위험 & 테스트 어려움)
- Lombok `@RequiredArgsConstructor`를 적극 활용한다.

## 2.3 RESTful API 설계
- 표준 HTTP 메서드 사용: GET/POST/PUT/PATCH/DELETE
- 엔드포인트는 리소스 단위로 설계한다. (`/users/{id}`)
- 대량 조회 시 페이징·정렬 옵션 포함 (`Pageable`)
- DTO를 사용해 Entity 직접 노출 금지
- 요청 데이터 검증: `@Valid`, `@NotNull`, `@Size` 등 활용

## 2.4 오류 처리
- `@ControllerAdvice` 기반 전역 예외 처리 사용
- 예외 메시지에는 민감 정보 포함 금지
- 명확한 에러 코드 체계 유지
- HTTP 상태 코드를 일관성 있게 사용하는 기준:
    - 200 OK: 성공 응답
    - 201 Created: 생성 성공
    - 400 Bad Request: 잘못된 요청
    - 401 Unauthorized: 인증 실패
    - 403 Forbidden: 인가 실패
    - 404 Not Found: 자원 없음
    - 500 Internal Server Error: 서버 오류

## 2.5 로깅
- 로그 레벨 수준을 명확히 구분:
    - DEBUG: 디버그용 상세 정보
    - INFO: 주요 비즈니스 흐름
    - WARN: 예상 가능한 문제
    - ERROR: 예외, 치명적 문제
- 주민번호, 전화번호 등 민감 정보는 절대 로그에 남기지 않는다.
- 요청/응답 전문은 필요 시 마스킹 처리 후 기록한다.

## 2.6 보안
- 환경 변수/Configuration으로 민감 정보를 관리한다.
- SQL Injection 방지를 위해 Prepared Query 또는 JPA 사용
- XSS, CSRF에 대비한 Spring Security 활용
- 입력값 검증을 강화하여 공격 가능성 차단

---

# 3. 테스트 코드 가이드

## 3.1 단위 테스트 (Unit Test)
- Service 계층 중심으로 작성한다.
- Repository, 외부 API 등은 Mocking 처리 (Mockito, MockBean 활용)
- 테스트 구조는 *Given → When → Then* 패턴을 따른다.
- 비즈니스 로직의 성공/실패 케이스 모두 검증한다.

## 3.2 통합 테스트 (Integration Test)
- Controller + Service 흐름 테스트 수행
- `@SpringBootTest`, `MockMvc` 사용 권장
- 실제 Bean 생성 기반 테스트로 환경 전체 검증

## 3.3 테스트 커버리지
- 핵심 기능은 80% 이상 커버리지를 목표로 한다.
- 중요한 로직은 반드시 테스트로 보장한다.

## 3.4 테스트 네이밍 규칙
- 테스트 메서드 이름은 동작을 설명하는 문장형으로 작성  
  예: `shouldReturnUserProfile_WhenUserExists()`

---

# 4. 코드 작성 스타일

## 4.1 네이밍 컨벤션
- 클래스: PascalCase
- 메서드/변수: camelCase
- 상수: UPPER_SNAKE_CASE
- 패키지: 소문자 + 단어 구분 없음

## 4.2 포맷팅
- Import 정리 (IDE 자동정렬)
- 불필요한 공백 제거
- 메서드 사이 한 줄 간격 유지

---

# 5. 리뷰 체크 기준 (Gemini Code Assist 점검 기준)

## 5.1 구조적 품질
- Controller에 비즈니스 로직이 포함되어 있지 않은가?
- Service가 지나치게 커져 SRP를 위반하지 않았는가?
- Repository는 JPA 규칙을 따르고 있는가?

## 5.2 코드 품질
- 네이밍은 의도를 충분히 설명하는가?
- 중복 코드가 남아있는가?
- 모듈 간 결합도가 과도하지 않은가?

## 5.3 안전성
- NPE 가능성은 없는가?
- Optional 사용이 적절한가?
- 유효성 검사가 모든 입력값에 적용되는가?

## 5.4 테스트
- 정상/예외 케이스가 모두 테스트되었는가?
- Mocking이 과하거나 부족하진 않은가?
- 테스트가 문서처럼 읽히는가?

---

# 6. 문서화
- API 스펙은 Apidog/Swagger로 제공한다.
- 복잡한 로직은 별도 문서로 기록한다.
- 주석은 “왜 이 로직이 필요한지”를 중심으로 작성한다.

---

# 7. 자동 리뷰 제외 기준
다음 파일/폴더는 리뷰 대상에서 제외됩니다:
- build/, target/, out/, .gradle/
- **/*.class
- 생성된 파일, 자동화된 리소스

---

본 스타일 가이드는 프로젝트 진행 과정에서 필요에 따라 업데이트될 수 있습니다.
