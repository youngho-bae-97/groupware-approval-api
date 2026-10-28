-- =====================================================================
-- 성능 테스트 데이터 정리 (01_generate_data.sql 로 만든 데이터만 삭제)
-- 시퀀스(next_val)는 되돌리지 않음 (앱이 발급한 ID 와 겹치지 않도록 유지)
-- =====================================================================

SET SESSION foreign_key_checks = 0;

DELETE FROM tbl_endaprline WHERE endaprline_id >= 10000000 AND endaprline_id < 30000000;
DELETE FROM tbl_enddoc WHERE enddoc_id >= 10000000 AND enddoc_id < 30000000;
DELETE FROM tbl_aprline WHERE aprline_id >= 10000000 AND aprline_id < 30000000;
DELETE FROM tbl_activedoc WHERE activedoc_id >= 10000000 AND activedoc_id < 30000000;
DELETE FROM tbl_document_master WHERE document_master_id >= 10000000 AND document_master_id < 30000000;
DELETE FROM tbl_user_master WHERE login_id LIKE 'perf\_user\_%';

DROP TABLE IF EXISTS tbl_doc_all;
DROP TABLE IF EXISTS tbl_aprline_all;
DROP TABLE IF EXISTS tbl_perf_num;

SET SESSION foreign_key_checks = 1;
