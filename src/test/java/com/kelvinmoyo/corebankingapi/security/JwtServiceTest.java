package com.kelvinmoyo.corebankingapi.security;

import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static String testSecret(byte fillByte) {
        byte[] keyBytes = new byte[64];
        java.util.Arrays.fill(keyBytes, fillByte);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    @Test
    void generateToken_thenExtractEmail_returnsOriginalEmail() {
        JwtService service = new JwtService(testSecret((byte) 1), 900000L);
        String token = service.generateToken("alice@example.com", "CUSTOMER");

        assertEquals("alice@example.com", service.extractEmail(token));
    }

    @Test
    void generateToken_thenExtractRole_returnsOriginalRole() {
        JwtService service = new JwtService(testSecret((byte) 1), 900000L);
        String token = service.generateToken("admin@example.com", "ADMIN");

        assertEquals("ADMIN", service.extractRole(token));
    }

    @Test
    void freshToken_isNotExpired() {
        JwtService service = new JwtService(testSecret((byte) 1), 900000L);
        String token = service.generateToken("alice@example.com", "CUSTOMER");

        assertFalse(service.isTokenExpired(token));
    }

    @Test
    void tokenWithNegativeExpiration_isAlreadyExpired() {
        JwtService service = new JwtService(testSecret((byte) 1), -10000L);
        String token = service.generateToken("alice@example.com", "CUSTOMER");

        assertTrue(service.isTokenExpired(token));
    }

    @Test
    void tokenSignedWithDifferentSecret_failsSignatureVerification() {
        JwtService serviceA = new JwtService(testSecret((byte) 1), 900000L);
        JwtService serviceB = new JwtService(testSecret((byte) 2), 900000L);
        String token = serviceA.generateToken("alice@example.com", "CUSTOMER");

        assertThrows(SignatureException.class, () -> serviceB.extractEmail(token));
    }

    @org.junit.jupiter.api.Test
    void isTokenValid_rejectsMismatchedEmail() {
        JwtService service = new JwtService(testSecret((byte) 1), 900000L);
        String token = service.generateToken("alice@example.com", "CUSTOMER");

        assertFalse(service.isTokenValid(token, "someone-else@example.com"));
        assertTrue(service.isTokenValid(token, "alice@example.com"));
    }
}
