package com.byh.groupware.global.security;

import com.byh.groupware.domain.user.entity.UserMaster;
import com.byh.groupware.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 로그인 시 AuthenticationManager(DaoAuthenticationProvider)가 호출
 * - 비밀번호 비교는 PasswordEncoder(BCrypt)로 Spring Security 가 수행
 */
@Service
@RequiredArgsConstructor
public class LoginUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        UserMaster user = userRepository.findByLoginId(loginId);
        if (user == null) {
            throw new UsernameNotFoundException("존재하지 않는 사용자: " + loginId);
        }
        return LoginUser.from(user);
    }
}
