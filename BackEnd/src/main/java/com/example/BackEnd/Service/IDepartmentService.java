package com.example.BackEnd.Service;

import com.example.BackEnd.DTO.DepartmentDTO;
import com.example.BackEnd.Entity.Department;
import com.example.BackEnd.Response.DepartmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IDepartmentService {
    Page<DepartmentResponse> getAllDepartments(String keyword, PageRequest pageRequest);
    Department addDepartment(DepartmentDTO departmentDTO, String file);
    void deleteDepartmentById(Integer id);
}
