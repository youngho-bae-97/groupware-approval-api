-- =====================================================================
-- EndDocumentMaster 제거 마이그레이션 2단계 (1단계의 unmatched = 0 확인 후 실행)
-- =====================================================================

-- 이전 참조(FK, 유니크 키, 컬럼) 제거 후 새 참조에 유니크 키 추가 (FK 는 두지 않음)
ALTER TABLE tbl_enddoc DROP FOREIGN KEY FKdj10u3k0jffdrbriamlcoax46;
ALTER TABLE tbl_enddoc
    DROP INDEX UKo4g8nek42qvg03wof3bnr1dqa,
    DROP COLUMN end_document_master_id,
    MODIFY COLUMN document_master_id BIGINT NOT NULL,
    ADD UNIQUE KEY uk_enddoc_document_master_id (document_master_id);

-- 완료 문서 마스터 테이블은 백업용으로 이름만 변경
RENAME TABLE tbl_end_document_master TO tbl_end_document_master_bak,
             tbl_end_document_master_seq TO tbl_end_document_master_seq_bak;

SHOW CREATE TABLE tbl_enddoc;
