package com.byh.groupware.global.security;

import com.byh.groupware.domain.user.entity.UserMaster;
import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 인증된 사용자 정보 (SecurityContext 의 principal, 세션에 저장됨)
 * - 엔티티(UserMaster) 대신 식별에 필요한 최소 정보만 가짐
 *   (detached 엔티티의 지연 로딩 문제, 세션 직렬화 크기, 개인정보 노출을 피하기 위함)
 * - 비밀번호 해시는 인증 직후 eraseCredentials() 로 지워져 세션에 남지 않음
 * - 문서 단위 권한(결재선 포함 여부, 현재 결재 차례)은 서비스에서 판단하므로 역할(authorities)은 두지 않음
 */
@Getter
public class LoginUser implements UserDetails, CredentialsContainer {

    private final Long id;
    private final String loginId;
    private final String name;
    private final boolean enabled;
    private String password;

    private LoginUser(Long id, String loginId, String name, String password, boolean enabled) {
        this.id = id;
        this.loginId = loginId;
        this.name = name;
        this.password = password;
        this.enabled = enabled;
    }

    public static LoginUser from(UserMaster user) {
        return new LoginUser(user.getId(), user.getLoginId(), user.getMemName(), user.getMemPass(),
                "Y".equals(user.getUseYn()));
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}
