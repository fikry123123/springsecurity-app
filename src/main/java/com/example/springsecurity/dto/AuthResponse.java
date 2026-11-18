package com.example.springsecurity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AuthResponse {
    private int status;         // HTTP Status Code (200, 401, dll)
    private String message;     // Pesan Login Sukses/Gagal
    private String token;       // JWT Token (jika sukses)
    private String refreshToken;
    private String email;       // Info user
    private String firstName;
    private String lastName;
    private String role;
    private LocalDateTime timestamp; // Waktu saat response dikirim
}
