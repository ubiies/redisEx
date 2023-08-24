package com.example.redisex.global.jwt;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import com.example.redisex.user.dto.CustomUserDetails;
import com.example.redisex.user.service.UserService;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/*
    jwt 생성, 해석 유효성 검즞 및 관련된 작업
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class JwtTokenProvider {
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.token.access-expiration-time}")
    private Long accessExpirationTime;

    @Value("${jwt.token.refresh-expiration-time}")
    private Long refreshExpirationTime;


    /*
          사용자 인증 정보를 바탕으로 Access 토큰 생성하여 일시적인 접근 권한 부여
     */
    public String createAccessToken(Authentication authentication){
        // Authentication 객체로부터 사용자 이름을 추출하여 토큰의 주제로 설정
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        // 현재 시간과 만료 시간 설정
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + accessExpirationTime);
        // 토큰을 서명하여 반환
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /*
        사용자 인증 정보를 기반으로 Refresh 토큰을 생성하고 redis에 저장
        refresh 토큰은 access 토큰의 만료 후 새로운 access 토큰을 발급받을 때 사용
     */
    public String createRefreshToken(Authentication authentication){
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + refreshExpirationTime);

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        // redis에 저장, 만료 시간도 함께 저장
        redisTemplate.opsForValue().set(
                authentication.getName(),
                refreshToken,
                refreshExpirationTime,
                TimeUnit.MILLISECONDS
        );
        return refreshToken;
    }


    /*
        토큰으로부터 클레임을 만들고, 이를 통해 user 객체 생성해 Authentication 객체 변환
        토큰을 복호화해 토큰에 들어있는 유저 정보를 꺼내고 authentication으로 반환
     */
    public Authentication getAuthentication(String token) {
        // jwt 토큰 분석하여 사용자 이름 추출
        String userPrincipal = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJwt(token)
                .getBody().getSubject();
        // CustomUserDetails 객체를 가져와 인증 객체 생성
        UserDetails userDetails = CustomUserDetails.builder().username(userPrincipal).build();
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }


    /*
        HTTP 요청 헤더에서 jwt 토큰을 추출하여 반환
        추출된 토큰 값을 반환하여 다른 메서드에서 사용할 수 있도록 해줌
     */
    public String resolveToken(HttpServletRequest req) {
        // 토큰을 bearer {토큰값} 형식으로 "Authoriaztion" 헤더에 포함
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // access 토큰을 검증
    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}
