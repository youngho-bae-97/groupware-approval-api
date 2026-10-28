-- =====================================================================
-- EndDocumentMaster 제거 마이그레이션
--  - 문서 마스터는 tbl_document_master 하나만 유지 (최종결재 후에도 삭제하지 않음)
--  - 완료 문서(tbl_enddoc)는 end_document_master_id 대신 document_master_id 로 마스터를 참조
--  - tbl_end_document_master 는 삭제하지 않고 _bak 으로 이름만 변경 (확인 후 직접 DROP)
-- =====================================================================

-- [1단계] 새 참조 컬럼 추가 및 값 채우기 (문서번호 doc_id 로 매칭)
ALTER TABLE tbl_enddoc ADD COLUMN document_master_id BIGINT NULL;

UPDATE tbl_enddoc e
JOIN tbl_end_document_master edm ON edm.end_document_master_id = e.end_document_master_id
JOIN tbl_document_master dm ON dm.doc_id = edm.doc_id
SET e.document_master_id = dm.document_master_id;

-- 매칭되지 않은 완료 문서가 0건이어야 2단계 진행
SELECT COUNT(*) AS unmatched FROM tbl_enddoc WHERE document_master_id IS NULL;
