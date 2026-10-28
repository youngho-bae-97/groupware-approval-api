# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

그룹웨어 **전자결재(기안 → 결재 → 완료/반려)** 백엔드. Spring Boot 3.5 / Java 17 / Maven / MySQL 8 / Spring Data JPA + QueryDSL 5 (jakarta). 코드 주석과 커밋 메시지는 한국어로 작성한다.

현재 **MyBatis → JPA 전환이 진행 중**이다. README.md는 전환 이전(MyBatis, `TBL_STATUS_MAP` 기반) 설계를 설명하고 있어 실제 코드와 다를 수 있다. 코드를 기준으로 판단할 것.

## 명령어

```bash
./mvnw spring-boot:run                       # 실행 (localhost:8080, Swagger: /swagger-ui/index.html)
./mvnw clean compile                         # 빌드 (QueryDSL Q클래스 → target/generated-sources/java)
./mvnw test                                  # 전체 테스트
./mvnw test -Dtest=JPAUserServiceTest        # 단일 테스트 클래스
./mvnw test -Dtest=JPAUserServiceTest#메서드명  # 단일 테스트 메서드
```

- 로컬 MySQL `localhost:3306/groupware_db` (계정 `gw_user`)이 떠 있어야 앱 실행과 테스트가 모두 동작한다. 별도의 테스트 프로파일이나 인메모리 DB는 없다.
- `ddl-auto: none`이라 스키마는 DB에서 직접 관리한다. 엔티티를 바꾸면 테이블 DDL도 따로 반영해야 한다.
- 테스트는 `@SpringBootTest` + `@Transactional` + `@Rollback(false)` 형태라 **실제 DB에 커밋된다**.
- 동시성 테스트(`FinalApprovalDeadlockTest`)는 스레드마다 커밋해야 해서 `@Transactional`을 쓰지 않고, `@AfterEach`에서 이번 실행에서 만든 데이터만 지운다. 동시 스레드 수는 운영 최대 동시성(`server.tomcat.threads.max`)과 같게 둔다.
- 커넥션 풀 크기는 `Tn × (Cm − 1) + 1`로 산정한다(`application.yaml`). Tn은 Tomcat 최대 스레드 수이고, Cm은 2다. ID 생성(`@GeneratedValue` AUTO → `*_seq` 테이블)이 트랜잭션 커넥션과 별도로 커넥션을 하나 더 쓰기 때문이다. 그래서 Tomcat 스레드 수를 바꾸면 풀 크기도 함께 바꿔야 한다. 풀이 부족하면 커넥션 고갈로 30초 동안 멈춘다(`docs/connection-pool/`).
- `DataInitConfig`(CommandLineRunner)가 기동 시 회사/부서/문서양식(FORM-01, FORM-02) 기초 데이터가 비어 있으면 넣는다.

## 아키텍처

패키지 구성: `domain/{approval,user,dept,company}/{controller,service,repository,entity,dto,exception,mapper}` + `global/{config,entity,exception}`.

### JPA와 레거시 MyBatis 공존
- 활성 빈: `JpaApprovalService`, `JpaApproveAction`, `JPAUserService`.
- `ApprovalServiceImpl`, `ApproveAction`, `RejectAction`, `UserServiceImpl`, `StatusMap`/`EndStatusMap`, `JpaApprovalServiceTest`는 **전체가 주석 처리된 레거시**다. `mapper/*.java`와 `resources/mapper/*.xml`이 남아 있지만 `application.yaml`의 mybatis 설정은 주석 처리되어 있다.
- 새 작업은 JPA 쪽에 한다. 레거시 코드는 참고용이다.

### 인증
- `SecurityConfig`는 모든 요청을 `permitAll`로 열어 두고 CSRF를 끈 상태다. 실제 인증은 **HttpSession**으로 한다. `UserController`가 로그인 시 `session.setAttribute("loginUser", UserMaster)`로 저장하고, 각 컨트롤러가 `session.getAttribute("loginUser")`로 꺼낸다.
- 세션의 `UserMaster`는 detached 상태라서 서비스에서 `userRepository.findByLoginId(...)`로 다시 조회한 뒤 사용한다.

### 결재 처리: 전략 패턴
- `ApprovalAction` 인터페이스(`getActionType()`, `doProcess()`)를 구현한 빈들을 `JpaApprovalService`가 `@PostConstruct`에서 `EnumMap<ApprovalType, ApprovalAction>`으로 등록하고, 요청의 `approveType`으로 골라 위임한다.
- `ApprovalType`: APPROVE / REJECT / FORWARD. 현재 활성 구현체는 `JpaApproveAction`(APPROVE) 하나뿐이다. 새 결재 유형은 `@Service`로 `ApprovalAction`을 구현하면 자동 등록된다.

### 문서 생명주기와 Hot/Cold 테이블 분리
- **진행(hot)**: `DocumentMaster`(문서의 "출생신고서", 1:1) → `ActiveDoc`(본문, 문서 상태) → `AprLine`(결재선, 1:N).
- **완료(cold)**: `EndDocumentMaster` → `EndDoc` → `EndAprLine`.
- 상태 관리는 원래 `StatusMap`이 담당했지만 최근 개편에서 제거됐고, 지금은 `ActiveDoc.docStatus`와 `AprLine`의 결재 상태가 그 역할을 한다.
- 최종 결재 시 `JpaApproveAction.archiveDocument()`가 `@EntityGraph`로 fetch한 hot 엔티티를 cold 엔티티로 복사해 저장한 뒤, hot 데이터를 **JPQL 벌크 삭제**한다(`AprLine` → `ActiveDoc` 순서, `@Modifying(flushAutomatically, clearAutomatically)`). orphanRemoval은 결재선 수만큼 단건 DELETE가 나가서 바꿨으므로 `removeActiveDoc()`을 같이 호출하지 말 것. `DocumentMaster` 자체는 남는다.
- DB에 FK 제약을 두지 않는다. 그래서 FK 컬럼 인덱스가 자동으로 생기지 않으니, 조건절에 쓰는 컬럼은 인덱스를 직접 만들어야 한다. `tbl_aprline.activedoc_id` 인덱스가 없으면 서로 다른 문서의 최종결재가 동시에 일어날 때 벌크 삭제가 풀 스캔하면서 데드락이 난다(`FinalApprovalDeadlockTest`, `docs/deadlock/`). 인덱스는 엔티티 `@Table(indexes=...)`에도 기록해 둔다.
- 상세 조회(`getApprovalDetail`)는 `docStatus == "03"`이면 `EndDoc`을, 그 외에는 `ActiveDoc`을 조회한다.
- 연관관계 편의 메서드(`addActiveDoc`, `addAprLine`, `addEndDoc` …)와 정적 팩토리(`createXxx`)로 엔티티를 생성하고, 저장은 루트(`DocumentMaster` / `EndDocumentMaster`)에서 cascade로 한다.

### 동시성 제어
- **문서번호 채번**: `NoMasterRepository.findLockByYearAndDocFormId`에서 `PESSIMISTIC_WRITE`를 건다. 번호 형식은 `연도 + 양식ID + 5자리 시퀀스`(예: `2026FORM-0100001`)이고, 해당 연도/양식 레코드가 없으면 먼저 생성한 뒤 락을 잡는다.
- **최종 결재**: `ActiveDocRepository.findByIdForUpdate`로 비관적 락을 잡은 뒤 상태를 다시 검증한다.
- **일반 결재**: `ActiveDoc.version`(`@Version`)으로 낙관적 락을 건다. 일반결재는 `ActiveDoc` 값이 바뀌지 않아 UPDATE가 나가지 않으므로, `em.lock(activeDoc, OPTIMISTIC_FORCE_INCREMENT)`으로 version을 강제로 올린다. 이미 영속성 컨텍스트에 있는 엔티티에는 `@Lock` 쿼리로 다시 조회해도 락 모드가 적용되지 않는다(실측). 그래서 리포지토리 쿼리가 아니라 `em.lock`을 쓴다. 충돌하면 `GlobalExceptionHandler`가 409를 응답한다.
- 최종 결재에는 force increment를 쓰지 않는다. 커밋 시점에 version UPDATE가 나가는데, 그 전에 같은 행을 벌크 삭제하기 때문이다.
- `ActiveDoc.version`(낙관적 락)과 `DocumentMaster.version`(재기안 문서 버전)은 서로 다른 개념이다.

### 상태 코드 (문자열)
- 문서 상태 `docStatus`: `01` 임시저장, `02` 진행중, `03` 완료, `04` 반려.
- 결재선 유형 `AprLine.approveType`: `01` 기안, `02` 결재, `03` 합의, `04` 반려, `09` 참조.
- 결재선 상태 `AprLine.approveStatus`(approveType과 별개의 필드): `01` 대기, `02` 진행중(현재 결재 차례), `03` 완료. 결재 시 현재 결재선은 `03`, 다음 결재선은 `02`가 된다.

### 기타
- 공통 감사 필드: `BaseTimeEntity` / `BaseEntity` (`@EnableJpaAuditing`).
- 예외는 도메인별 `exception` 패키지에 두고, `GlobalExceptionHandler`가 `ErrorResponse`로 변환한다.
- SQL 로깅: p6spy와 `org.hibernate.SQL`/bind TRACE가 켜져 있다.
