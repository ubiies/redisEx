package com.example.redisex.user.service;

import com.example.redisex.global.jwt.JwtTokenDto;
import com.example.redisex.global.jwt.JwtTokenProvider;
import com.example.redisex.user.dto.CustomUserDetails;
import com.example.redisex.user.dto.JoinDto;
import com.example.redisex.user.dto.LoginDto;
import com.example.redisex.user.entity.UserEntity;
import com.example.redisex.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
@Service
public class UserService implements UserDetailsManager {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    // Join
    public void createUser(CustomUserDetails user) {
        if (userRepository.existsByUsername(user.getUsername()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("%s 는 이미 사용중인 아이디 입니다.", user.getUsername()));
        if (userRepository.existsByEmail(user.getEmail()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("%s 는 이미 사용중인 이메일 입니다.", user.getEmail()));
        if (userRepository.existsByPhone(user.getPhone()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("%s 는 이미 사용중인 전화번호 입니다.", user.getPhone()));
        try {
            user.setEncodedPassword(passwordEncoder.encode(user.getPassword()));
            this.userRepository.save(UserEntity.fromUserDetails(user));
        } catch (ClassCastException e) {
            log.error("Exception message : {} | failed to cast to {}",e.getMessage(), CustomUserDetails.class);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Login
    @Transactional
    public JwtTokenDto login(LoginDto request) {
        CustomUserDetails user = this.loadUserByUsername(request.getUsername());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        // UsernamePasswordAuthenticationToken 객체 생성
        // spring security 인증 프로세스를 위해 사용됨
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                user.getPassword());
        // 토큰 생성
        JwtTokenDto response = new JwtTokenDto(
                jwtTokenProvider.createAccessToken(authentication),
                jwtTokenProvider.createRefreshToken(authentication)
        );
        return response;
    }

    // UserDetails와 Authentication의 패스워드를 비교하고 검증하는 로직을 처리
    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) throw new UsernameNotFoundException(username);
        return CustomUserDetails.fromEntity(optionalUser.get());
    }


    @Override
    public void createUser(UserDetails user) {

    }

    @Override
    public void updateUser(UserDetails user) {

    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public boolean userExists(String username) {
        return true;
    }

}
