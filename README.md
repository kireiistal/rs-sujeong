# 공지사항 관리 시스템

이 프로젝트는 공지사항을 등록, 수정, 삭제, 조회할 수 있는 REST API를 제공하는 시스템입니다.

## 기술 스택

- **언어**: Java 17
- **프레임워크**: Spring Boot 3.4.3
- **데이터베이스**: PostgreSQL 14.7
- **ORM**: JPA(Hibernate) + QueryDSL
- **마이그레이션**: Flyway
- **API 문서화**: Springdoc OpenAPI (Swagger)
- **파일 저장**: 로컬 파일 시스템
- **빌드 도구**: Gradle 8.13

## 주요 기능

- 공지사항 등록, 수정, 삭제, 조회 API
- 첨부파일 업로드 및 다운로드
- 공지사항 검색 (제목, 제목+내용)
- 검색 기간 필터링
- 페이지네이션 지원

## 문제 해결 전략

### 1. 아키텍처 설계

```
com.rsupport.rs_sujeong
├── config              # 애플리케이션 설정 클래스
├── controller          # REST API 컨트롤러
├── domain              # 도메인 모델 및 비즈니스 로직
│   └── notice          # 공지사항 관련 클래스
│       ├── dto         # 데이터 전송 객체
│       ├── entity      # 엔티티 클래스
│       └── repository  # 데이터 액세스 레이어
├── exception           # 사용자 정의 예외 클래스
└── handler             # 전역 예외 처리
```

- **계층 구조**: Controller → Service → Repository 패턴을 사용하여 관심사 분리
- **도메인 중심 설계**: 핵심 비즈니스 로직을 도메인 레이어에 집중
- **데이터 접근 계층**: JPA와 QueryDSL을 조합하여 유연한 쿼리 구성

### 2. 데이터 모델링

- **논리적 삭제(Soft Delete)**: `@SQLDelete`와 `@SQLRestriction`을 사용하여 데이터 삭제 시 물리적으로 삭제하지 않고 논리적으로 삭제 처리
- **엔티티 관계**: 공지사항(Notice)과 첨부파일(NoticeFile) 간 1:N 관계 설정
- **감사 정보**: 생성/수정 시간, 생성자 정보 자동 관리를 위한 `AuditingEntityListener` 활용

### 3. 검색 최적화

- **동적 쿼리**: QueryDSL을 활용한 동적 검색 조건 구성
- **검색 타입**: 제목만 검색 또는 제목+내용 검색 옵션 제공
- **기간 검색**: 시작일과 종료일 기준 필터링
- **페이지네이션**: Spring Data의 Pageable 활용

### 4. 파일 처리 전략

- **파일 저장**: UUID를 활용한 고유 파일명 생성으로 충돌 방지
- **메타데이터 관리**: 원본 파일명, 저장 파일명, 파일 크기, MIME 타입 등 메타데이터 보존
- **첨부파일 수정**: 기존 파일 삭제 및 새 파일 추가 로직 구현
- **파일 다운로드**: 원본 파일명 유지 및 인코딩 처리

### 5. 대용량 트래픽 대응

- **읽기 최적화**: 조회 작업에 `@Transactional(readOnly = true)` 적용
- **캐시 처리**: 캐시를 활용하여 목록 조회시 쿼리가 매번 날아 가는것을 방지
- **N+1 문제 해결**: 적절한 Fetch 전략 및 쿼리 최적화
- **인덱스 설계**: 검색 조건에 맞는 인덱스 구성
- **JPA 최적화**: `@DynamicInsert`, `@DynamicUpdate`를 통한 불필요한 쿼리 감소

### 6. 예외 처리

- **글로벌 예외 처리**: `@RestControllerAdvice`를 통한 일관된 에러 응답 형식 제공
- **검증 예외 처리**: Bean Validation 실패에 대한 명확한 에러 메시지

## API 문서

OpenAPI(Swagger) 문서는 애플리케이션 실행 후 다음 URL에서 확인 가능합니다:

```
http://localhost:8080/swagger-ui.html
```

### 주요 API 엔드포인트

| HTTP 메서드 | 경로                             | 설명              |
|----------|--------------------------------|-----------------|
| GET      | /api/v1/notices                | 공지사항 목록 조회 및 검색 |
| POST     | /api/v1/notices                | 공지사항 등록         |
| GET      | /api/v1/notices/{id}           | 공지사항 상세 조회      |
| PUT      | /api/v1/notices/{id}           | 공지사항 수정         |
| DELETE   | /api/v1/notices/{id}           | 공지사항 삭제         |
| GET      | /api/v1/notices/files/{fileId} | 첨부파일 다운로드       |

## 실행 방법

### 필수 조건

- JDK 17 이상
- Docker 및 Docker Compose (데이터베이스 실행용)

### 데이터베이스 실행

```bash
# 프로젝트 루트 디렉토리에서
docker-compose up -d
```

### 애플리케이션 빌드 및 실행

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

또는 JAR 파일 직접 실행:

```bash
java -jar build/libs/rs-sujeong-0.0.1-SNAPSHOT.jar
```

## 성능 최적화

- **캐싱 전략**: 자주 조회되는 데이터에 대한 캐싱 도입
- **데이터베이스 최적화**: 적절한 인덱스 및 쿼리 튜닝
- **조회수 업데이트 최적화**: 별도 쿼리로 조회수만 증가시키는 기능 구현
- **페이지네이션 최적화**: 대용량 데이터에 효율적인 페이지 처리

## 향후 개선 사항

- **캐싱 확장**: Redis 등을 활용한 분산 캐싱 도입
- **파일 저장소 확장**: 클라우드 스토리지(S3 등) 연동
- **사용자 인증/인가**: Spring Security 도입
- **성능 테스트**: JMeter 등을 통한 부하 테스트 및 성능 최적화

## 단위/통합 테스트

주요 기능에 대한 단위 테스트와 통합 테스트가 구현되어 있습니다:

```bash
# 테스트 실행
./gradlew test
```
