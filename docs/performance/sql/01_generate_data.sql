-- =====================================================================
-- 성능 테스트용 대량 데이터 생성
--  - 사용자 1,000명 / 완료 문서 1,000,000건 / 진행 문서 10,000건
--  - 결재선은 문서당 3~5줄 (약 404만 줄)
--  - 성능 테스트 데이터는 모두 ID 10,000,000 이상, 사용자 login_id 는 perf_ 접두어
--  - 정리: 99_cleanup.sql
--
-- 생성 순서
--  1) 분리 전 구조를 재현한 통합 테이블(tbl_doc_all, tbl_aprline_all)에 전체 데이터 생성
--  2) 통합 테이블에서 진행(hot) / 완료(cold) 테이블로 나눠 복사
--     -> 두 구조가 완전히 같은 데이터를 가지도록 보장
--
-- 통합 테이블은 CREATE TABLE ... LIKE 로 만들어 진행 테이블과 인덱스가 동일함 (공정한 비교)
-- =====================================================================

SET SESSION cte_max_recursion_depth = 2000000;
SET SESSION foreign_key_checks = 0;

-- 숫자 테이블 (1 ~ 1,010,000)
DROP TABLE IF EXISTS tbl_perf_num;
CREATE TABLE tbl_perf_num (n INT NOT NULL PRIMARY KEY);
INSERT INTO tbl_perf_num
WITH RECURSIVE r(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM r WHERE n < 1010000)
SELECT n FROM r;

-- 사용자 1,000명 (mem_id 10000001 ~ 10001000)
INSERT INTO tbl_user_master (mem_id, dept_id, dept_name, login_id, mem_name, mem_pass, use_yn, job_grade)
SELECT 10000000 + n, 1, '백엔드개발팀', CONCAT('perf_user_', LPAD(n, 4, '0')), CONCAT('성능사용자', n), 'x', 'Y', '사원'
FROM tbl_perf_num
WHERE n <= 1000;

-- ---------------------------------------------------------------------
-- 1) 통합 테이블 (분리 전 구조)
--    문서 n <= 1,000,000 : 완료(03), n > 1,000,000 : 진행(02, 가장 최근 문서)
--    작성일은 2021-01-01 부터 150초 간격 (약 4.8년치)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS tbl_doc_all;
CREATE TABLE tbl_doc_all LIKE tbl_activedoc;
DROP TABLE IF EXISTS tbl_aprline_all;
CREATE TABLE tbl_aprline_all LIKE tbl_aprline;

INSERT INTO tbl_doc_all (activedoc_id, document_master_id, created_date, last_modified_date, attach_yn, urgent_yn,
                         doc_status, doc_title, search_content, version, created_by)
SELECT 10000000 + n,
       10000000 + n,
       TIMESTAMP('2021-01-01') + INTERVAL (n * 150) SECOND,
       TIMESTAMP('2021-01-01') + INTERVAL (n * 150) SECOND,
       'N', 'N',
       IF(n > 1000000, '02', '03'),
       CONCAT('성능테스트 문서 ', n),
       CONCAT('성능테스트 본문 ', n),
       0, 'perf'
FROM tbl_perf_num;

-- 결재선: 문서당 3 + (n % 3) 줄, 1단계는 기안자(01), 나머지는 결재(02)
--  - 완료 문서: 전부 완료(03)
--  - 진행 문서: 현재 차례 단계 c = 2 + n % (줄 수 - 1) -> c 이전 완료(03), c 진행중(02), c 이후 대기(01)
--  - 기안자 mem_id = 10000001 + (n * 7919 % 1000), 결재자 mem_id = 10000001 + ((n * 31 + 단계 * 7919) % 1000)
INSERT INTO tbl_aprline_all (aprline_id, activedoc_id, step_seq, mem_id, approve_type, approve_status, approver_name,
                             created_date, last_modified_date, created_by)
SELECT 10000000 + d.n * 10 + k.n,
       10000000 + d.n,
       k.n,
       IF(k.n = 1, 10000001 + MOD(d.n * 7919, 1000), 10000001 + MOD(d.n * 31 + k.n * 7919, 1000)),
       IF(k.n = 1, '01', '02'),
       CASE
           WHEN d.n <= 1000000 THEN '03'
           WHEN k.n < 2 + MOD(d.n, 2 + MOD(d.n, 3)) THEN '03'
           WHEN k.n = 2 + MOD(d.n, 2 + MOD(d.n, 3)) THEN '02'
           ELSE '01'
       END,
       CONCAT('성능사용자', IF(k.n = 1, 1 + MOD(d.n * 7919, 1000), 1 + MOD(d.n * 31 + k.n * 7919, 1000))),
       TIMESTAMP('2021-01-01') + INTERVAL (d.n * 150) SECOND,
       TIMESTAMP('2021-01-01') + INTERVAL (d.n * 150) SECOND,
       'perf'
FROM tbl_perf_num d
JOIN tbl_perf_num k ON k.n <= 3 + MOD(d.n, 3)
WHERE d.n <= 340000;

INSERT INTO tbl_aprline_all (aprline_id, activedoc_id, step_seq, mem_id, approve_type, approve_status, approver_name,
                             created_date, last_modified_date, created_by)
SELECT 10000000 + d.n * 10 + k.n,
       10000000 + d.n,
       k.n,
       IF(k.n = 1, 10000001 + MOD(d.n * 7919, 1000), 10000001 + MOD(d.n * 31 + k.n * 7919, 1000)),
       IF(k.n = 1, '01', '02'),
       CASE
           WHEN d.n <= 1000000 THEN '03'
           WHEN k.n < 2 + MOD(d.n, 2 + MOD(d.n, 3)) THEN '03'
           WHEN k.n = 2 + MOD(d.n, 2 + MOD(d.n, 3)) THEN '02'
           ELSE '01'
       END,
       CONCAT('성능사용자', IF(k.n = 1, 1 + MOD(d.n * 7919, 1000), 1 + MOD(d.n * 31 + k.n * 7919, 1000))),
       TIMESTAMP('2021-01-01') + INTERVAL (d.n * 150) SECOND,
       TIMESTAMP('2021-01-01') + INTERVAL (d.n * 150) SECOND,
       'perf'
FROM tbl_perf_num d
JOIN tbl_perf_num k ON k.n <= 3 + MOD(d.n, 3)
WHERE d.n > 340000 AND d.n <= 680000;

INSERT INTO tbl_aprline_all (aprline_id, activedoc_id, step_seq, mem_id, approve_type, approve_status, approver_name,
                             created_date, last_modified_date, created_by)
SELECT 10000000 + d.n * 10 + k.n,
       10000000 + d.n,
       k.n,
       IF(k.n = 1, 10000001 + MOD(d.n * 7919, 1000), 10000001 + MOD(d.n * 31 + k.n * 7919, 1000)),
       IF(k.n = 1, '01', '02'),
       CASE
           WHEN d.n <= 1000000 THEN '03'
           WHEN k.n < 2 + MOD(d.n, 2 + MOD(d.n, 3)) THEN '03'
           WHEN k.n = 2 + MOD(d.n, 2 + MOD(d.n, 3)) THEN '02'
           ELSE '01'
       END,
       CONCAT('성능사용자', IF(k.n = 1, 1 + MOD(d.n * 7919, 1000), 1 + MOD(d.n * 31 + k.n * 7919, 1000))),
       TIMESTAMP('2021-01-01') + INTERVAL (d.n * 150) SECOND,
       TIMESTAMP('2021-01-01') + INTERVAL (d.n * 150) SECOND,
       'perf'
FROM tbl_perf_num d
JOIN tbl_perf_num k ON k.n <= 3 + MOD(d.n, 3)
WHERE d.n > 680000;

-- ---------------------------------------------------------------------
-- 2) 분리 구조 (현재 구조): 통합 테이블에서 복사
-- ---------------------------------------------------------------------
-- 문서 마스터: 진행/완료 모두 (문서의 출생신고서 역할이라 이관 후에도 남음)
INSERT INTO tbl_document_master (document_master_id, version, created_date, last_modified_date, docform_id, mem_id,
                                 approval_form_title, doc_id, drafter_dept, drafter_name, root_doc_id,
                                 security_level, preserve_year, created_by)
SELECT document_master_id, 1, created_date, created_date, 1,
       10000001 + MOD((document_master_id - 10000000) * 7919, 1000),
       '연차신청서', CONCAT('PERF-', document_master_id), '백엔드개발팀',
       CONCAT('성능사용자', 1 + MOD((document_master_id - 10000000) * 7919, 1000)),
       CONCAT('PERF-', document_master_id), 'B', '5', 'perf'
FROM tbl_doc_all;

-- 진행(hot)
INSERT INTO tbl_activedoc SELECT * FROM tbl_doc_all WHERE doc_status = '02';
INSERT INTO tbl_aprline SELECT * FROM tbl_aprline_all WHERE activedoc_id > 11000000;

-- 완료(cold)
-- 완료 문서는 문서 마스터(tbl_document_master)를 그대로 참조
INSERT INTO tbl_enddoc (enddoc_id, document_master_id, archive_date, created_date, last_modified_date, attach_yn,
                        urgent_yn, doc_status, doc_title, search_content, created_by)
SELECT activedoc_id, document_master_id, created_date + INTERVAL 3 DAY, created_date, created_date, attach_yn,
       urgent_yn, '03', doc_title, search_content, 'perf'
FROM tbl_doc_all
WHERE doc_status = '03';

-- 완료 결재선은 결재자를 mem_id 가 아닌 login_id(approver_id) 로 박제
INSERT INTO tbl_endaprline (endaprline_id, enddoc_id, step_seq, approver_id, approve_type, approve_status,
                            approver_name, created_date, last_modified_date, created_by)
SELECT aprline_id, activedoc_id, step_seq, CONCAT('perf_user_', LPAD(mem_id - 10000000, 4, '0')), approve_type,
       approve_status, approver_name, created_date, last_modified_date, 'perf'
FROM tbl_aprline_all
WHERE activedoc_id <= 10500000;

INSERT INTO tbl_endaprline (endaprline_id, enddoc_id, step_seq, approver_id, approve_type, approve_status,
                            approver_name, created_date, last_modified_date, created_by)
SELECT aprline_id, activedoc_id, step_seq, CONCAT('perf_user_', LPAD(mem_id - 10000000, 4, '0')), approve_type,
       approve_status, approver_name, created_date, last_modified_date, 'perf'
FROM tbl_aprline_all
WHERE activedoc_id > 10500000 AND activedoc_id <= 11000000;

-- 앱이 새로 발급하는 ID 가 성능 테스트 데이터와 겹치지 않도록 시퀀스를 뒤로 이동
UPDATE tbl_user_master_seq SET next_val = 30000000;
UPDATE tbl_document_master_seq SET next_val = 30000000;
UPDATE tbl_activedoc_seq SET next_val = 30000000;
UPDATE tbl_aprline_seq SET next_val = 30000000;
UPDATE tbl_enddoc_seq SET next_val = 30000000;
UPDATE tbl_endaprline_seq SET next_val = 30000000;

SET SESSION foreign_key_checks = 1;

ANALYZE TABLE tbl_user_master, tbl_document_master, tbl_activedoc, tbl_aprline,
    tbl_enddoc, tbl_endaprline, tbl_doc_all, tbl_aprline_all;

SELECT 'doc_all' t, COUNT(*) cnt FROM tbl_doc_all
UNION ALL SELECT 'aprline_all', COUNT(*) FROM tbl_aprline_all
UNION ALL SELECT 'document_master', COUNT(*) FROM tbl_document_master
UNION ALL SELECT 'activedoc', COUNT(*) FROM tbl_activedoc
UNION ALL SELECT 'aprline', COUNT(*) FROM tbl_aprline
UNION ALL SELECT 'enddoc', COUNT(*) FROM tbl_enddoc
UNION ALL SELECT 'endaprline', COUNT(*) FROM tbl_endaprline;
