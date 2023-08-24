package com.example.redisex.global.jwt;

import com.example.redisex.global.exception.BaseException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.example.redisex.global.exception.ErrorCode.INVALID_JWT;
import static com.example.redisex.global.exception.ErrorCode.REDIS_ERROR;

@Slf4j
@Component
@DependsOn("jwtTokenProvider")
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
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
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth); // 정상 토큰이면 SecurityContext에 저장
            }
        } catch (RedisConnectionFailureException e) {
            SecurityContextHolder.clearContext();
            throw new BaseException(REDIS_ERROR);
        } catch (Exception e) {
            throw new BaseException(INVALID_JWT);
        }

        filterChain.doFilter(request, response);
        /*
            코드 요청이 들어올 때마다 jwt 토큰을 검증하고, 유효한 토큰이면 사용자의 인증 정보를
            SecurityContext에 저장하여 Spring Security의 인증과 인가 과정 활용
         */
    }
}

