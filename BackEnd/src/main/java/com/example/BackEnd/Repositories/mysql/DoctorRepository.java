package com.example.BackEnd.Repositories.mysql;

import com.example.BackEnd.Entity.Doctor;
import com.example.BackEnd.Entity.User;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

    @Query("""
            SELECT d FROM Doctor d
            WHERE (:keyword = '' OR LOWER(d.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:departmentId IS NULL OR d.department.id = :departmentId)
            AND (:status IS NULL OR d.isAvailable = :status)
        """)
    Page<Doctor> searchDoctors(
            @Param("keyword") String keyword,
            @Param("departmentId") Integer departmentId,
            @Param("status") Boolean status,
            Pageable pageable);

    @Query("SELECT d FROM Doctor d WHERE d.department.id = :departmentId")
    Page<Doctor> findByDepartmentId(@Param("departmentId") Integer id, Pageable pageable);

    Optional<Doctor> findByUser(User user);
}
