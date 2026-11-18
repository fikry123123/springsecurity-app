package com.example.springsecurity.controller;

import com.example.springsecurity.entities.Manager;
import com.example.springsecurity.entities.User;
import com.example.springsecurity.repository.ManagerRepository;
import com.example.springsecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/managers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ManagerController {

    private final ManagerRepository managerRepository;
    private final UserRepository userRepository;

    // ✅ CREATE - Tambah Manager baru
    @PostMapping
    public ResponseEntity<?> createManager(@RequestBody Map<String, Object> request) {
        String department = (String) request.get("department");
        Long userId;

        try {
            userId = Long.valueOf(request.get("userId").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "userId tidak valid."));
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User dengan ID " + userId + " tidak ditemukan."));
        }

        Manager manager = new Manager();
        manager.setDepartment(department);
        manager.setUser(userOptional.get());

        Manager savedManager = managerRepository.save(manager);
        return ResponseEntity.ok(savedManager);
    }

    // ✅ READ - Ambil semua Manager
    @GetMapping
    public ResponseEntity<?> getAllManagers() {
        List<Manager> managers = managerRepository.findAll();
        return ResponseEntity.ok(managers);
    }

    // ✅ READ - Ambil Manager berdasarkan ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getManagerById(@PathVariable Long id) {
        Optional<Manager> managerOpt = managerRepository.findById(id);
        if (!managerOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Manager dengan ID " + id + " tidak ditemukan."));
        }

        return ResponseEntity.ok(managerOpt.get());
    }

    // ✅ UPDATE - Update departemen Manager
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @RequestBody Manager updatedManager) {
        Optional<Manager> managerOpt = managerRepository.findById(id);
        if (!managerOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Manager dengan ID " + id + " tidak ditemukan."));
        }

        Manager manager = managerOpt.get();
        manager.setDepartment(updatedManager.getDepartment());
        Manager saved = managerRepository.save(manager);

        return ResponseEntity.ok(saved);
    }

    // ✅ DELETE - Hapus Manager
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteManager(@PathVariable Long id) {
        if (!managerRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Manager dengan ID " + id + " tidak ditemukan."));
        }

        managerRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Manager dengan ID " + id + " berhasil dihapus."));
    }
}
