package com.example.BackEnd.Service.impl;

import com.example.BackEnd.DTO.DepartmentDTO;
import com.example.BackEnd.Entity.Department;
import com.example.BackEnd.Repositories.mysql.DepartmentRepository;
import com.example.BackEnd.Response.DepartmentResponse;
import com.example.BackEnd.Service.IDepartmentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class DepartmentService implements IDepartmentService {
    private final DepartmentRepository departmentRepository;

    @Override
    public Page<DepartmentResponse> getAllDepartments(String keyword, PageRequest pageRequest) {
        Page<Department> departmentPage = departmentRepository.searchDepartments(keyword, pageRequest);
        return departmentPage.map(DepartmentResponse::fromDepartment);
    }

    @Override
    public Department addDepartment(DepartmentDTO departmentDTO, String file) {
        Department department = Department.builder()
                .name(departmentDTO.getName())
                .description(departmentDTO.getDescription())
                .thumbnailUrl(file)
                .isAvailable(true)
                .build();
        return departmentRepository.save(department);
    }

    @Override
    public void deleteDepartmentById(Integer id) {
        Department department = departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found Department"));
        if(department.isAvailable() == true) department.setAvailable(false);
        else department.setAvailable(true);
        departmentRepository.save(department);
    }

}
