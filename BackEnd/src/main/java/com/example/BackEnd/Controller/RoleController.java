package com.example.BackEnd.Controller;

import com.example.BackEnd.Entity.Role;
import com.example.BackEnd.Service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/roles")
@RequiredArgsConstructor
public class RoleController {
    private final IRoleService iRoleService;

    @GetMapping("/")
    public ResponseEntity<?> getAllRoles() {
        List<Role> roles = iRoleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
}
