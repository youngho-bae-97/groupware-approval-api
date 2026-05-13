package com.byh.groupware.domain.user.service;

import com.byh.groupware.domain.user.dto.UserRegisterDTO;
import com.byh.groupware.domain.user.entity.UserMaster;
import com.byh.groupware.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class JPAUserServiceTest {

    @Autowired
    private JPAUserService jpaUserService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void registerTest() throws Exception {
//        "memId": "minsoo123",
//        "memPass": "1234",
//        "memName": "김민수",
//        "deptCode": "DEPT01",
//        "coCode": "CO001"
        //given
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setCoCode("CO001");
        userRegisterDTO.setDeptCode("DEPT01");
        userRegisterDTO.setMemName("김민수");
        userRegisterDTO.setMemId("minsoo123");
        userRegisterDTO.setMemPass("1234");

        //when
        jpaUserService.register(userRegisterDTO);

        UserMaster user = userRepository.findByLoginId("minsoo123");
        //then
        Assertions.assertThat(user.getMemName()).isEqualTo("김민수");

    }


}