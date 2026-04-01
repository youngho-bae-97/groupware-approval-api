package com.byh.groupware.domain.approval.mapper;

import com.byh.groupware.domain.approval.model.DocumentMaster;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApprovalMapper {
    DocumentMaster selectDraftDocument();

}
