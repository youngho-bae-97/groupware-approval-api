package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.entity.StatusMap;
import com.byh.groupware.domain.approval.repository.StatusMapRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class JpaApprovalServiceTest {

    @Autowired
    StatusMapRepository statusMapRepository;

    @Test
    public void docListTODO() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdDate"));
        //when
        Page<StatusMap> maps = statusMapRepository.docToDoList(pageable, "minsoo123");
        //then
        Assertions.assertThat(maps.getSize()).isEqualTo(1);

    }

}