package com.kelvinmoyo.corebankingapi.controller;

import com.kelvinmoyo.corebankingapi.dto.LoginRequest;
import com.kelvinmoyo.corebankingapi.dto.LoginResponse;
import com.kelvinmoyo.corebankingapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Login and JWT issuance")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Authenticate and receive a JWT",
            description = "Verifies email and password via BCrypt, then returns a signed JWT (15-minute expiry) embedding the customer's email and role. Returns an identical 401 for a wrong password or a nonexistent email, to prevent account enumeration.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
