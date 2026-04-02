package com.byh.groupware.domain.user.service;

import com.byh.groupware.domain.user.mapper.UserMapper;
import com.byh.groupware.domain.user.model.UserMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void register(UserMaster user) {
        // 1. 비밀번호 암호화 (1234 -> $2a$10$...)
        String encodedPw = passwordEncoder.encode(user.getMemPass());
        user.setMemPass(encodedPw);

        // 2. DB Insert
        userMapper.insertMember(user);

    }

    @Override
    public UserMaster login(String memId, String rawPassword) {

        UserMaster user = userMapper.findById(memId);

        if (user == null || !passwordEncoder.matches(rawPassword, user.getMemPass())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        user.setMemPass(null); // 보안상 비번은 지우고 반환
        return user;
    }
}
