package com.byh.groupware.domain.user.service;

import com.byh.groupware.domain.user.model.UserMaster;

public interface UserService {
     void register(UserMaster user);
     UserMaster login(String memId, String rawPassword);
}
