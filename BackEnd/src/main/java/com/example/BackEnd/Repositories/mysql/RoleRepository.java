package com.example.BackEnd.Repositories.mysql;

import com.example.BackEnd.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
