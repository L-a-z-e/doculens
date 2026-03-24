package com.doculens.domain.auth.service;

import com.doculens.domain.auth.dto.AuthResponse;
import com.doculens.domain.auth.dto.LoginRequest;
import com.doculens.domain.auth.dto.RegisterRequest;
import com.doculens.domain.auth.entity.User;
import com.doculens.domain.auth.repository.UserRepository;
import com.doculens.global.error.exception.DuplicateResourceException;
import com.doculens.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("이미 사용 중인 이메일입니다: " + request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .build();

        User saved = userRepository.save(user);
        String accessToken = jwtTokenProvider.createAccessToken(saved.getId(), saved.getEmail());

        return new AuthResponse(accessToken, saved.getId(), saved.getEmail(), saved.getName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());

        return new AuthResponse(accessToken, user.getId(), user.getEmail(), user.getName());
    }
}
