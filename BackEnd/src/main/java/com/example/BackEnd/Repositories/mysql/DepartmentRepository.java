package com.example.BackEnd.Repositories.mysql;

import com.example.BackEnd.Entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    @Query("SELECT p FROM Department p WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR p.name LIKE CONCAT('%', :keyword, '%')) AND p.isAvailable = true")
    Page<Department> searchDepartments(@Param("keyword") String keyword, Pageable pageable);

}
