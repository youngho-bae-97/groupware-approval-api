package com.byh.groupware.domain.user.service;

import com.byh.groupware.domain.dept.entity.DeptMaster;
import com.byh.groupware.domain.dept.repository.DeptRepository;
import com.byh.groupware.domain.user.dto.UserRegisterDTO;
import com.byh.groupware.domain.user.entity.UserMaster;
import com.byh.groupware.domain.user.exception.InvalidLoginException;
import com.byh.groupware.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JPAUserService {
    private final UserRepository userRepository;
    private final DeptRepository deptRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void register(UserRegisterDTO userRegisterDTO) {
        // 1. 비밀번호 암호화 (1234 -> $2a$10$...)
        String encodedPw = passwordEncoder.encode(userRegisterDTO.getMemPass());

        DeptMaster dept = deptRepository.findByDeptCode(userRegisterDTO.getDeptCode());

        UserMaster user = new UserMaster(encodedPw,dept,userRegisterDTO);
        //user.EncryptPassword(encodedPw);
//        user.setMemPass(encodedPw);

        // 2. DB Insert
//        userMapper.insertMember(user);
        userRepository.save(user);


    }

    public UserMaster login(String memId, String rawPass) {


        UserMaster user = userRepository.findByLoginId(memId);
        if(user == null || !passwordEncoder.matches(rawPass, user.getMemPass())){
            throw new InvalidLoginException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        user.cleanMemPass();
        return user;
    }
}
