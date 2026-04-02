package com.byh.groupware.domain.user.service;

import com.byh.groupware.domain.user.model.UserMasterVO;

public interface UserService {
     void register(UserMasterVO user);
     UserMasterVO login(String memId, String rawPassword);
}
