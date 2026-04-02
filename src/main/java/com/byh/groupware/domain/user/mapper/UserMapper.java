package com.byh.groupware.domain.user.mapper;

import com.byh.groupware.domain.user.model.UserMasterVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    void insertMember(UserMasterVO user);

    UserMasterVO findById(String memId);
}
