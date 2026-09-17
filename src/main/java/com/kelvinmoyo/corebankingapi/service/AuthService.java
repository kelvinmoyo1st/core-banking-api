package com.kelvinmoyo.corebankingapi.service;

import com.kelvinmoyo.corebankingapi.dto.LoginRequest;
import com.kelvinmoyo.corebankingapi.dto.LoginResponse;
import com.kelvinmoyo.corebankingapi.entity.Customer;
import com.kelvinmoyo.corebankingapi.exception.InvalidCredentialsException;
import com.kelvinmoyo.corebankingapi.repository.CustomerRepository;
import com.kelvinmoyo.corebankingapi.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), customer.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(customer.getEmail(), customer.getRole().name());
        return new LoginResponse(token);
    }
}
