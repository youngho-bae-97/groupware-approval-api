package com.byh.groupware.domain.user.dto;

import com.byh.groupware.domain.user.entity.UserMaster;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserLoginDTO {
    private String memId;
    private String memPass;

    public UserLoginDTO(UserMaster userMaster){
        this.memId = userMaster.getLoginId();


    }
}
