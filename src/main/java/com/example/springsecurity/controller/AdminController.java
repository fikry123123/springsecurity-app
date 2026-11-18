package com.example.springsecurity.controller;

import com.example.springsecurity.entities.Manager;
import com.example.springsecurity.entities.Role;
import com.example.springsecurity.entities.User;
import com.example.springsecurity.repository.ManagerRepository;
import com.example.springsecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    private final UserRepository userRepository;
    private final ManagerRepository managerRepository; // Tambahkan repository manager!
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @GetMapping
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello Admin");
    }


    @GetMapping("/users/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam("query") String query) {
        List<User> users = userRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query,
                        query, query);

        users.forEach(user -> {
            if (user.getProfilePicture() != null && !user.getProfilePicture().startsWith("http")) {
                String fileName = new File(user.getProfilePicture()).getName();
                user.setProfilePicture("http://localhost:8080/uploads/" + fileName);
            }
        });

        return ResponseEntity.ok(users);
    }

    @PostMapping(value = "/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createUser(@RequestPart("firstName") String firstName,
                                             @RequestPart("lastName") String lastName,
                                             @RequestPart("email") String email,
                                             @RequestPart(value = "file", required = false) MultipartFile file) {

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Nama dan email tidak boleh kosong.");
        }


        if (file != null && !isValidImageType(file)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Format file harus PNG atau JPG.");
        }

        String filePath = saveImage(file, null);

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode("default1"));
        user.setProfilePicture(filePath);

        Optional<Long> nextId = userRepository.findNextAvailableId();
        nextId.ifPresent(user::setId);

        User savedUser = userRepository.save(user);

        return ResponseEntity.ok("User berhasil dibuat.");
    }

    // Validasi format file hanya PNG dan JPG
    private boolean isValidImageType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("image/png") || contentType.equals("image/jpeg"));
    }


    @PutMapping(value = "/users/{id}/update-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateUserProfilePicture(@PathVariable Long id,
                                                           @RequestParam("file") MultipartFile file) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User dengan ID " + id + " tidak ditemukan.");
        }

        if (file != null && !isValidImageType(file)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Format file harus PNG atau JPG.");
        }


        User user = userOptional.get();
        String filePath = saveImage(file, user.getProfilePicture());

        user.setProfilePicture(filePath);
        userRepository.save(user);

        return ResponseEntity.ok("Foto profil berhasil diperbarui untuk user ID: " + id);
    }
    
    @GetMapping("/manager/{managerId}/users")
public ResponseEntity<?> getUsersByManager(@PathVariable Long managerId) {
    Manager manager = managerRepository.findById(managerId).orElse(null);

    if (manager == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Manager tidak ditemukan.");
    }

    List<User> users = userRepository.findByManager(manager);

    List<Map<String, Object>> userList = users.stream().map(user -> {
        Map<String, Object> u = new HashMap<>();
        u.put("id", user.getId());
        u.put("firstName", user.getFirstName());
        u.put("lastName", user.getLastName());
        u.put("email", user.getEmail());
        return u;
    }).toList();

    Map<String, Object> response = new HashMap<>();
    response.put("manager", Map.of(
        "id", manager.getId(),
        "department", manager.getDepartment()
    ));
    response.put("users", userList);

    return ResponseEntity.ok(response);
}

    

    @PutMapping("/assign-user")
    public ResponseEntity<?> assignUserToManager(@RequestBody Map<String, Long> payload) {
    Long userId = payload.get("userId");
    Long managerId = payload.get("managerId");

    if (userId == null || managerId == null) {
        return ResponseEntity.badRequest().body("userId dan managerId tidak boleh null.");
    }

    User user = userRepository.findById(userId).orElse(null);
    Manager manager = managerRepository.findById(managerId).orElse(null);

    if (user == null || manager == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User atau Manager tidak ditemukan.");
    }

    user.setManager(manager);
    userRepository.save(user);

    return ResponseEntity.ok("User berhasil di-assign ke Manager.");
    }

    @PutMapping("/unassign")
public ResponseEntity<?> unassignManager(@RequestBody Map<String, Long> body) {
    Long userId = body.get("userId");

    if (userId == null) {
        return ResponseEntity.badRequest().body("userId harus dikirim.");
    }

    Optional<User> userOpt = userRepository.findById(userId);
    if (userOpt.isPresent()) {
        User user = userOpt.get();
        user.setManager(null);
        userRepository.save(user);
        return ResponseEntity.ok("Manager berhasil di-unassign dari user.");
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User tidak ditemukan.");
    }
}

@GetMapping("/users/with-manager")
public ResponseEntity<List<User>> getUsersWithManager() {
    List<User> usersWithManager = userRepository.findAll().stream()
        .filter(user -> user.getManager() != null)
        .toList();

    return ResponseEntity.ok(usersWithManager);
}


    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(required = false, defaultValue = "1") int page,  // Default ke 1
            @RequestParam(defaultValue = "10") int size) {

        if (page < 1) {
            return ResponseEntity.badRequest().build();  // Beri status 400 jika user mengirim page=0
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());
        Page<User> usersPage = userRepository.findAll(pageable);

        Page<User> modifiedUsersPage = usersPage.map(user -> {
            if (user.getProfilePicture() != null) {
                user.setProfilePicture("http://localhost:8080/uploads/" + new File(user.getProfilePicture()).getName());
            }
            return user;
        });

        return ResponseEntity.ok(modifiedUsersPage);
    }


    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User dengan ID " + id + " tidak ditemukan."));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setFirstName(userDetails.getFirstName());
            user.setLastName(userDetails.getLastName());
            user.setEmail(userDetails.getEmail());
            return ResponseEntity.ok(userRepository.save(user));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User dengan ID " + id + " tidak ditemukan.");
        }

        User user = userOptional.get();
        if (user.getProfilePicture() != null) {
            deleteImage(user.getProfilePicture());
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok("User dengan ID " + id + " berhasil dihapus.");
    }

    private String saveImage(MultipartFile file, String oldFilePath) {
        if (file.isEmpty()) {
            return null;
        }

        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                throw new RuntimeException("Gagal membuat direktori untuk upload");
            }

            if (oldFilePath != null) {
                deleteImage(oldFilePath);
            }

            String filePath = UPLOAD_DIR + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            file.transferTo(new File(filePath));
            return filePath;

        } catch (IOException e) {
            throw new RuntimeException("Gagal mengunggah file: " + e.getMessage());
        }
    }

    private void deleteImage(String filePath) {
        File file = new File(filePath);
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("Gagal menghapus file: " + filePath);
        }
}
}