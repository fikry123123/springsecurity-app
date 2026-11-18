package com.example.springsecurity.service.impl;

import com.example.springsecurity.dto.AuthResponse;
import com.example.springsecurity.dto.RefreshTokenRequest;
import com.example.springsecurity.dto.SignUpRequest;
import com.example.springsecurity.dto.SigninRequest;
import com.example.springsecurity.entities.Role;
import com.example.springsecurity.entities.User;
import com.example.springsecurity.repository.UserRepository;
import com.example.springsecurity.service.AuthenticationService;
import com.example.springsecurity.service.JWTService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    @Override
    public User signup(SignUpRequest signUpRequest) {
        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        if (signUpRequest.getProfilePicture() != null && !signUpRequest.getProfilePicture().isEmpty()) {
            String filePath = "uploads/" + signUpRequest.getProfilePicture().getOriginalFilename();
            try {
                signUpRequest.getProfilePicture().transferTo(new File(filePath));
                user.setProfilePicture(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Gagal mengunggah gambar profil");
            }
        }

        return userRepository.save(user);
    }


    @Override
    public AuthResponse signin(SigninRequest signinRequest, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signinRequest.getEmail(),
                        signinRequest.getPassword()
                )
        );

        User user = userRepository.findByEmail(signinRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        String jwt = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);

        addJwtCookie(response, jwt);

        return new AuthResponse(
                HttpStatus.OK.value(),
                "Login berhasil",
                jwt,
                refreshToken,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                LocalDateTime.now()
        );
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest, HttpServletRequest request, HttpServletResponse response) {
        String token = extractJwtFromRequestOrCookies(refreshTokenRequest, request);

        if (token == null) {
            throw new IllegalArgumentException("Refresh token is missing");
        }

        String userEmail = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (jwtService.isTokenValid(token, user)) {
            String newJwt = jwtService.generateToken(user);
            addJwtCookie(response, newJwt);

            return new AuthResponse(
                    HttpStatus.OK.value(),
                    "Token refreshed successfully",
                    newJwt,
                    token,
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole().name(),
                    LocalDateTime.now()
            );
        }

        throw new IllegalArgumentException("Invalid refresh token");
    }

    private void addJwtCookie(HttpServletResponse response, String token) {
        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);

        jwtCookie.setSecure(false); // Set to true in production with HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600);
        response.addCookie(jwtCookie);
    }

    private String extractJwtFromRequestOrCookies(RefreshTokenRequest refreshTokenRequest, HttpServletRequest request) {
        String token = null;

        if (refreshTokenRequest != null && refreshTokenRequest.getToken() != null && !refreshTokenRequest.getToken().isEmpty()) {
            token = refreshTokenRequest.getToken();
        }

        if (token == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        return token;
    }
}