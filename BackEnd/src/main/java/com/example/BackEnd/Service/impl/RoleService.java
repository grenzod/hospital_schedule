package com.example.BackEnd.Service.impl;

import com.example.BackEnd.Entity.Role;
import com.example.BackEnd.Repositories.mysql.RoleRepository;
import com.example.BackEnd.Service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {
    private final RoleRepository roleRepository;

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
