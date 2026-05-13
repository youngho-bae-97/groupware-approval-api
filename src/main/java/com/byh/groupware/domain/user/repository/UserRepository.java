package com.byh.groupware.domain.user.repository;

import com.byh.groupware.domain.user.entity.UserMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserMaster, Long> {

    UserMaster findByLoginId(String memId);
}
