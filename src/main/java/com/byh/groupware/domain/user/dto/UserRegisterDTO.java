package com.byh.groupware.domain.user.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String memId;
    private String memPass;
    private String memName;
    private String deptCode;
    private String coCode;
}
