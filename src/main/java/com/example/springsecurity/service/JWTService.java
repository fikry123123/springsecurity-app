package com.example.springsecurity.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface JWTService {

    JWTService jwtService();

    String extractUsername(String token);

    String generateToken(UserDetails userDetails); // ✅ Perbaikan: Gunakan UserDetails

    boolean isTokenValid(String token, UserDetails userDetails);

    String generateRefreshToken(Map<String, Object> claims, UserDetails userDetails);
}
