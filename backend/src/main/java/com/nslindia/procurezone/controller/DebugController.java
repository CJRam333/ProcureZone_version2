package com.nslindia.procurezone.controller;

import com.nslindia.procurezone.identity.EmployeeRepository;
import com.nslindia.procurezone.identity.EmployeeRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DebugController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeRoleRepository employeeRoleRepository;

    @GetMapping("/api/test/db")
    public Map<String, Object> testDb() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("employeeCount", employeeRepository.count());
            response.put("employees", employeeRepository.findAll().stream().limit(5).map(e -> Map.of("id", e.getEmployeeNumber(), "name", e.getFullName(), "email", e.getEmail(), "status", e.getStatus())).toList());
            // Just return counts to be safe
            response.put("rolesCount", employeeRoleRepository.count());
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }
}
