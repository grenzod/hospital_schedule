package com.example.BackEnd.Service.impl;

import com.example.BackEnd.Entity.Department;
import com.example.BackEnd.Entity.Doctor;
import com.example.BackEnd.Entity.User;
import com.example.BackEnd.Repositories.mysql.DepartmentRepository;
import com.example.BackEnd.Repositories.mysql.DoctorRepository;
import com.example.BackEnd.Repositories.mysql.RoleRepository;
import com.example.BackEnd.Repositories.mysql.UserRepository;
import com.example.BackEnd.Response.DoctorResponse;
import com.example.BackEnd.Service.IDoctorService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class DoctorService implements IDoctorService {
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;

    @Override
    public Page<DoctorResponse> getAllDoctors(String keyword, Integer departmentId, Boolean status, PageRequest pageRequest) {
        Page<Doctor> page = doctorRepository.searchDoctors(keyword, departmentId, status, pageRequest);
        return page.map(DoctorResponse::fromDoctor);
    }

    @Override
    public Page<DoctorResponse> getDoctorsByDepartmentId(Integer id, PageRequest pageRequest) {
        Page<Doctor> doctorPage = doctorRepository.findByDepartmentId(id, pageRequest);
        return doctorPage.map(DoctorResponse::fromDoctor);
    }

    @Override
    public Doctor addDoctor(Integer userId, Integer departmentId, String file, String licenseNumber) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        if(doctorRepository.findByUser(user) != null){
            throw new Exception("Had added to Doctor");
        }
        Doctor doctor = Doctor.builder()
                .user(user)
                .department(department)
                .licenseNumber(licenseNumber)
                .avatarUrl(file)
                .isAvailable(true)
                .build();
        user.setRole(roleRepository.findById(2).orElseThrow(() -> new RuntimeException("Role not found")));
        return doctorRepository.save(doctor);
    }

    @Override
    public void deleteDoctorById(Integer id) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new RuntimeException("doctor not found"));
        User user = doctor.getUser();
        if(doctor.getIsAvailable()) {
            doctor.setIsAvailable(false);
            user.setRole(roleRepository.findById(3).orElseThrow(() -> new RuntimeException("Role not found")));
        }
        else {
            doctor.setIsAvailable(true);
            user.setRole(roleRepository.findById(2).orElseThrow(() -> new RuntimeException("Role not found")));
        }
        doctorRepository.save(doctor);
    }

    @Override
    public DoctorResponse getDoctorById(Integer id) {
        return doctorRepository.findById(id)
            .map(DoctorResponse::fromDoctor)
            .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

}
