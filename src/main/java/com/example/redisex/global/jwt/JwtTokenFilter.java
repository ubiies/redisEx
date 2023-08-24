package com.example.redisex.global.jwt;

import com.example.redisex.user.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
@DependsOn("jwtTokenProvider")
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtTokenUtils jwtTokenUtils;
    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenFilter(JwtTokenUtils jwtTokenUtils, JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenUtils = jwtTokenUtils;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    /*
        요청을 처리하고 SecurityContext에 인증 정보를 저장하는 메서드
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 요청에서 jwt 토큰을 추출
        String token = jwtTokenProvider.resolveToken(request);
        try {
            // 추출한 토큰이 유효한 토큰인지 검증
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // 유효한 경우 사용자 이름을 가져와서 SecurityContext에 인증 정보 저장
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                String username = jwtTokenUtils.parseClaims(token).getSubject();
                // UsernamePasswordAuthenticationToken 객체를 생성하여 사용자 정보와
                // 토큰, 빈 권한 목록을 전달
                Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
                        CustomUserDetails.builder().username(username).build(),
                        token, new ArrayList<>()
                );
                context.setAuthentication(authenticationToken);
                // securityContext에 저장된 인증 정보 생성
                SecurityContextHolder.setContext(context);
                log.info("Set security context with JWT");
            } else {
                log.warn("JWT validation failed");
            }
        } catch (Exception e) {
            log.warn("JWT validation failed");
        }
        filterChain.doFilter(request, response);
        /*
            코드 요청이 들어올 때마다 jwt 토큰을 검증하고, 유효한 토큰이면 사용자의 인증 정보를
            SecurityContext에 저장하여 Spring Security의 인증과 인가 과정 활용
         */
    }
}

