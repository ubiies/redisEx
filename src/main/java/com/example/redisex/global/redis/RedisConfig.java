package com.example.redisex.global.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;


/*
    redis 를 효율적으로 사용하기 위해 Bean 들을 구성하는 클래스
 */

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.port}")
    private int port;
    @Value("${spring.data.redis.host}")
    private String host;

    // Redis 서버와의 연결을 생성하는 Bean 정의 메서드
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // LettuceConnectionFactory 클래스를 사용하여 Redis 서버와 연결
        // Lettuce : redis 클라이언트 라이브러리 중 하나
        return new LettuceConnectionFactory();
    }

    // Redis를 조작하기 위한 RedisTemlate의 Bean을 정의하는 메서드
    // RedisTemplate : CRUD 작업 및 다양한 Redis 명령을 실행하는데 사
    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();

        // String 타입의 키와 값에 직렬화를 위해 StringRedisSerializer 설정
        // redis-cli을 통해 직접 데이터 조회 시 알아볼 수 없는 형태로 출력되는 것을 방지
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        // 앞서 정의한 redisConnectionFactory() 메서드에서 생성한 연결 팩토리 할당
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        return redisTemplate;
    }
}
