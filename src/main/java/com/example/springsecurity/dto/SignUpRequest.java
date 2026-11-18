package com.example.springsecurity.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SignUpRequest {

    private String firstName;
    private String LastName;
    private String email;
    private String password;
    private MultipartFile profilePicture; // Tambahkan field ini

}
