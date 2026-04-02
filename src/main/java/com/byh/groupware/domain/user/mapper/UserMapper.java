package com.byh.groupware.domain.user.mapper;

import com.byh.groupware.domain.user.model.UserMaster;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    void insertMember(UserMaster user);

    UserMaster findById(String memId);
}
