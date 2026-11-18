package com.example.springsecurity.service;

import com.example.springsecurity.dto.AuthResponse;
import com.example.springsecurity.dto.RefreshTokenRequest;
import com.example.springsecurity.dto.SignUpRequest;
import com.example.springsecurity.dto.SigninRequest;
import com.example.springsecurity.entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {

    User signup(SignUpRequest signUpRequest);
    AuthResponse signin(SigninRequest signinRequest, HttpServletResponse response);
    AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest, HttpServletRequest request, HttpServletResponse response);
}
