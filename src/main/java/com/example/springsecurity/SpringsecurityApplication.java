package com.example.springsecurity;

import com.example.springsecurity.entities.Role;
import com.example.springsecurity.entities.User;
import com.example.springsecurity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
@SpringBootApplication
public class SpringsecurityApplication implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(SpringsecurityApplication.class, args);
        System.out.println("Server is Running");
    }

    public void run(String... args) {
        List<User> adminAccount = userRepository.findByRole(Role.ADMIN);
        if (adminAccount.isEmpty()) {
            User user = new User();
            user.setId(1L);
            user.setFirstName("Admin");
            user.setLastName("Admin");
            user.setEmail("admin@example.com");
            user.setRole(Role.ADMIN);
            user.setPassword(new BCryptPasswordEncoder().encode("admin"));

            userRepository.save(user);
        }
    }
}
