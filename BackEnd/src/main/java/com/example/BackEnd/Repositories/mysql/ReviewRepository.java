package com.example.BackEnd.Repositories.mysql;

import com.example.BackEnd.DTO.ObjectStatsDTO;
import com.example.BackEnd.Entity.Review;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    @Query("SELECT r FROM Review r WHERE r.doctor.id = :doctorId")
    Page<Review> findByDoctorId(
            @Param("doctorId") Integer doctorId,
            Pageable pageable);

    @Query("""
                SELECT new com.example.BackEnd.DTO.ObjectStatsDTO(
                        YEAR(u.createdAt),
                        MONTH(u.createdAt),
                        COUNT(u)
                )
                FROM Review u
                        WHERE u.createdAt >= :startDate
                        GROUP BY YEAR(u.createdAt), MONTH(u.createdAt)
                        ORDER BY YEAR(u.createdAt), MONTH(u.createdAt)
        """)
    List<ObjectStatsDTO> countNewCommentsPerMonthSince(@Param("startDate") LocalDateTime startDate);
}
