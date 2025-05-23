package com.example.BackEnd.Repositories.mysql;

import com.example.BackEnd.DTO.ObjectStatsDTO;
import com.example.BackEnd.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
        boolean existsByEmail(String email);

        Optional<User> findByEmail(String email);

        Optional<User> findByPhoneNumber(String phoneNumber);

        @Query("SELECT p FROM User p WHERE " +
                        "(:keyword IS NULL OR :keyword = '' OR p.fullName LIKE %:keyword% OR p.address LIKE %:keyword%) AND p.role.id = 3")
        Page<User> searchUsers(@Param("keyword") String keyword,
                        Pageable pageable);

        @Query("""
                SELECT new com.example.BackEnd.DTO.ObjectStatsDTO(
                        YEAR(u.createdAt),
                        MONTH(u.createdAt),
                        COUNT(u)
                )
                FROM User u
                        WHERE u.createdAt >= :startDate
                        GROUP BY YEAR(u.createdAt), MONTH(u.createdAt)
                        ORDER BY YEAR(u.createdAt), MONTH(u.createdAt)
        """)
        List<ObjectStatsDTO> countNewUsersPerMonthSince(@Param("startDate") LocalDateTime startDate);
}
