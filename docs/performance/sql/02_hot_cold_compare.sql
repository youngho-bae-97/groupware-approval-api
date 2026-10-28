-- =====================================================================
-- 진행/완료 테이블 분리 효과 측정
--  - 분리 구조: tbl_activedoc + tbl_aprline (진행 문서 1만 건, 결재선 약 4만 줄)
--  - 통합 구조: tbl_doc_all + tbl_aprline_all (진행 + 완료 101만 건, 결재선 약 404만 줄)
--  - 두 구조는 같은 데이터, 같은 인덱스 (CREATE TABLE ... LIKE)
--  - 쿼리는 앱(JPQL)이 실제로 생성하는 SQL 과 같은 형태
--  - 측정 사용자: perf_user_0001
--  - 각 쿼리는 버퍼 풀을 데우기 위해 한 번 실행한 뒤 EXPLAIN ANALYZE 로 측정
-- =====================================================================

-- 테이블/인덱스 크기
SELECT table_name, table_rows,
       ROUND(data_length / 1024 / 1024, 1)  AS data_mb,
       ROUND(index_length / 1024 / 1024, 1) AS index_mb
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('tbl_activedoc', 'tbl_aprline', 'tbl_doc_all', 'tbl_aprline_all')
ORDER BY table_name;

-- ---------------------------------------------------------------------
-- [1] 결재대기함 (docToDoList) - 목록
-- ---------------------------------------------------------------------
SELECT d.* FROM tbl_activedoc d
JOIN tbl_aprline l ON l.activedoc_id = d.activedoc_id
JOIN tbl_user_master u ON u.mem_id = l.mem_id
WHERE d.doc_status = '02' AND l.approve_status = '02' AND u.login_id = 'perf_user_0001'
ORDER BY d.created_date LIMIT 0, 10;

EXPLAIN ANALYZE
SELECT d.* FROM tbl_activedoc d
JOIN tbl_aprline l ON l.activedoc_id = d.activedoc_id
JOIN tbl_user_master u ON u.mem_id = l.mem_id
WHERE d.doc_status = '02' AND l.approve_status = '02' AND u.login_id = 'perf_user_0001'
ORDER BY d.created_date LIMIT 0, 10;

SELECT d.* FROM tbl_doc_all d
JOIN tbl_aprline_all l ON l.activedoc_id = d.activedoc_id
JOIN tbl_user_master u ON u.mem_id = l.mem_id
WHERE d.doc_status = '02' AND l.approve_status = '02' AND u.login_id = 'perf_user_0001'
ORDER BY d.created_date LIMIT 0, 10;

EXPLAIN ANALYZE
SELECT d.* FROM tbl_doc_all d
JOIN tbl_aprline_all l ON l.activedoc_id = d.activedoc_id
JOIN tbl_user_master u ON u.mem_id = l.mem_id
WHERE d.doc_status = '02' AND l.approve_status = '02' AND u.login_id = 'perf_user_0001'
ORDER BY d.created_date LIMIT 0, 10;

-- [1-2] 결재대기함 - 건수 (페이징 count 쿼리)
EXPLAIN ANALYZE
SELECT COUNT(d.activedoc_id) FROM tbl_activedoc d
JOIN tbl_aprline l ON l.activedoc_id = d.activedoc_id
JOIN tbl_user_master u ON u.mem_id = l.mem_id
WHERE d.doc_status = '02' AND l.approve_status = '02' AND u.login_id = 'perf_user_0001';

EXPLAIN ANALYZE
SELECT COUNT(d.activedoc_id) FROM tbl_doc_all d
JOIN tbl_aprline_all l ON l.activedoc_id = d.activedoc_id
JOIN tbl_user_master u ON u.mem_id = l.mem_id
WHERE d.doc_status = '02' AND l.approve_status = '02' AND u.login_id = 'perf_user_0001';

-- ---------------------------------------------------------------------
-- [2] 기안함 (docDraftList) - 목록
-- ---------------------------------------------------------------------
SELECT DISTINCT d.* FROM tbl_activedoc d
JOIN tbl_aprline l ON l.activedoc_id = d.activedoc_id
JOIN tbl_user_master u ON u.mem_id = l.mem_id
WHERE d.doc_status = '02' AND l.step_seq = 1 AND u.login_id = 'perf_user_0001'
ORDER BY d.created_date LIMIT 0, 10;

EXPLAIN ANALYZE
SELECT DISTINCT d.* FROM tbl_activedoc d
JOIN tbl_aprline l ON l.activedoc_id = d.activedoc_id
JOIN tbl_user_master u ON u.mem_id = l.mem_id
WHERE d.doc_status = '02' AND l.step_seq = 1 AND u.login_id = 'perf_user_0001'
ORDER BY d.created_date LIMIT 0, 10;

SELECT DISTINCT d.* FROM tbl_doc_all d
JOIN tbl_aprline_all l ON l.activedoc_id = d.activedoc_id
JOIN tbl_user_master u ON u.mem_id = l.mem_id
WHERE d.doc_status = '02' AND l.step_seq = 1 AND u.login_id = 'perf_user_0001'
ORDER BY d.created_date LIMIT 0, 10;

EXPLAIN ANALYZE
SELECT DISTINCT d.* FROM tbl_doc_all d
JOIN tbl_aprline_all l ON l.activedoc_id = d.activedoc_id
JOIN tbl_user_master u ON u.mem_id = l.mem_id
WHERE d.doc_status = '02' AND l.step_seq = 1 AND u.login_id = 'perf_user_0001'
ORDER BY d.created_date LIMIT 0, 10;

-- [2-2] 기안함 - 건수
EXPLAIN ANALYZE
SELECT COUNT(DISTINCT d.activedoc_id) FROM tbl_activedoc d
JOIN tbl_aprline l ON l.activedoc_id = d.activedoc_id
JOIN tbl_user_master u ON u.mem_id = l.mem_id
WHERE d.doc_status = '02' AND l.step_seq = 1 AND u.login_id = 'perf_user_0001';

EXPLAIN ANALYZE
SELECT COUNT(DISTINCT d.activedoc_id) FROM tbl_doc_all d
JOIN tbl_aprline_all l ON l.activedoc_id = d.activedoc_id
JOIN tbl_user_master u ON u.mem_id = l.mem_id
WHERE d.doc_status = '02' AND l.step_seq = 1 AND u.login_id = 'perf_user_0001';
