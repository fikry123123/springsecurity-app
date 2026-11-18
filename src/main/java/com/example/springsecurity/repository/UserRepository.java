package com.example.springsecurity.repository;

import com.example.springsecurity.entities.Manager;
import com.example.springsecurity.entities.Role;
import com.example.springsecurity.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
        
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String firstName, String lastName, String email);
    List<User> findByRole(Role role);
    @Query("SELECT u FROM User u WHERE u.manager IS NOT NULL")
List<User> findAllWithManager();
    List<User> findByManager(Manager manager);
    Optional<User> findById(Long id);
    Optional<User> findByProfilePicture(String profilePicture);
    Optional<User> findByEmail(String email);
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))")
    List<User> findByFirstName(@Param("firstName") String firstName);
    @Query("SELECT u FROM User u WHERE LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<User> findByLastName(@Param("lastName") String lastName);
    @Query("SELECT MAX(u.id) FROM User u")
    Optional<Long> getMaxId();
    @Query(value = "SELECT MIN(t1.id) + 1 FROM users t1 WHERE NOT EXISTS (SELECT 1 FROM users t2 WHERE t2.id = t1.id + 1)", nativeQuery = true)
    Optional<Long> findNextAvailableId();
        // 🔹 INNER JOIN: User yang memiliki manager (ORDER BY ASC)
        @Query("SELECT u.id, u.firstName, u.lastName, u.email, m.id, m.firstName, m.email " +
                "FROM User u INNER JOIN User m ON u.manager.id = m.id ORDER BY u.id ASC")
        List<Object[]> findUsersWithManager();

        // 🔹 LEFT JOIN: Semua user, termasuk yang tidak punya manager (ORDER BY ASC)
        @Query("SELECT u.id, u.firstName, u.lastName, u.email, m.id, m.firstName, m.email " +
                "FROM User u LEFT JOIN User m ON u.manager.id = m.id ORDER BY u.id ASC")
        List<Object[]> findUsersWithManagerLeftJoin();

        // 🔹 RIGHT JOIN: Semua manager, termasuk yang tidak punya user (ORDER BY ASC)
        @Query("SELECT u.id, u.firstName, u.lastName, u.email, m.id, m.firstName, m.email " +
                "FROM User u RIGHT JOIN User m ON u.manager.id = m.id ORDER BY u.id ASC")
        List<Object[]> findUsersWithManagerRightJoin();

        // 🔹 FULL JOIN: Gabungan LEFT JOIN & RIGHT JOIN (ORDER BY ASC)
        @Query("SELECT u.id, u.firstName, u.lastName, u.email, m.id, m.firstName, m.email " +
                "FROM User u FULL JOIN User m ON u.manager.id = m.id ORDER BY u.id ASC")
        List<Object[]> findUsersWithManagerFullJoin();
    }