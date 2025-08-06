package com.adriano.order.controller;

import com.adriano.order.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/token")
    public ResponseEntity<String> gerarToken(@RequestParam String username) {
        return ResponseEntity.ok(jwtTokenProvider.gerarToken(username));
    }
}
