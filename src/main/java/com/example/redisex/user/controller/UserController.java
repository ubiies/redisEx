package com.example.redisex.user.controller;

import com.example.redisex.global.jwt.JwtTokenDto;
import com.example.redisex.user.dto.LoginDto;
import com.example.redisex.user.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private final UserService service;

    @PostMapping("/login")
    public JwtTokenDto login(@RequestBody @Valid LoginDto request) {
        return service.login(request);
    }

}
