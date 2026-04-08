# 📑 전자결재 시스템 백엔드 복기 프로젝트

본 프로젝트는 실제 그룹웨어 유지보수(SM) 경험을 바탕으로, 기존 시스템의 비효율적인 구조를 개선하고 비즈니스 로직을 재설계한 **백엔드 중심 복기 프로젝트**입니다.

---

## 1. 프로젝트 개요
* **목적**: 실무 결재 프로세스(기안-결재-완료) 구현 및 대용량 데이터를 고려한 DB 설계 연습
* **핵심 기능**:
    * 전자결재 기안 및 승인/반려 프로세스
    * 문서 상태(진행/완료)에 따른 물리적 테이블 분리 조회
    * TBL_STATUS_MAP(상태관리) 테이블을 별도로 구성하여 락경합을 줄이고 조회 성능향상을 도모
    * MyBatis 기반의 1:N 결재선 객체 매핑
    * 사용자 권한에 따른 동적 버튼 활성화 로직

---

## 2. 기술 스택 (Tech Stack)
* **Framework**: Spring Boot 3.x
* **Language**: Java 17
* **Database**: MySQL 8.0
* **ORM**: MyBatis
* **Build Tool**: Maven
* **Documentation**: Swagger (SpringDoc OpenAPI 2.3.0)
* **Security**: Spring Security

---

## 3. 핵심 설계 및 기술적 고민 (Technical Point)

### 3.1 효율적인 데이터 관리 (Table Partitioning Strategy)
* **고민**: 문서 데이터 누적 시 전체 조회 성능 저하 문제.
* **해결**: 진행 문서(`TBL_ACTIVEDOC`)와 완료 문서(`TBL_ENDDOC`)를 분리하여 설계. `STATUS_MAP` 테이블을 브릿지로 활용해 데이터의 생명주기를 관리하고 락경합을 줄이기 위해 상태관리에 필요한 가벼운 데이터 위주로 컬럼을 구성했습니다.

### 3.2 전략 패턴을 통한 결재로직 구현
* **고민**: 이전에 유지보수 당시 결재로직이 Switch case문으로 구성되어있어 결재코드자체가 매우 방대하고 유지보수가 어려워짐
* **해결**: 결재행위를 추상화하여 ApprovalAction이라는 인터페이스를 두고, 각 구현체에 결재로직을 작성토록 하여 유지보수 및 확장성을 향상시켰습니다.

### 3.3 비관적 락 활용
* **고민**: 문서에 대한 결재행위가 중첩될 경우 문서상태를 나타내는 데이터가 꼬여서 정합성이 깨지는 일 발생
* **해결**: 문서번호를 채번할 때와 문서상태값을 변경할 때 비관적 락을 걸도록 하여 데이터 꼬임을 방지 


---

## 4. API 명세 (Swagger)
SpringDoc을 도입하여 별도의 문서 작성 없이 API 스펙을 관리하고 테스트 환경을 구축했습니다.

* **UI 접속**: `http://localhost:8080/swagger-ui/index.html`
* **주요 API**:
  - `POST /approval/draft`: 결재 기안
  - `GET /approval/docDetail`: 문서 상세 및 결재선 조회
  - `POST /approval/process`: 승인/반려 처리

