package com.example.BackEnd.Service;

import com.example.BackEnd.Entity.Doctor;
import com.example.BackEnd.Response.DoctorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IDoctorService {
    Page<DoctorResponse> getAllDoctors(String keyword, Integer departmentId, Boolean status, PageRequest pageRequest);
    Page<DoctorResponse> getDoctorsByDepartmentId(Integer id, PageRequest pageRequest);
    Doctor addDoctor(Integer userId, Integer departmentId, String file, String licenseNumber) throws Exception;
    void deleteDoctorById(Integer id);
    DoctorResponse getDoctorById(Integer id);
}
