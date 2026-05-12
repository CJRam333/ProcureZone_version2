package com.nslindia.procurezone.controller;

import com.nslindia.procurezone.dto.RoleDto;
import com.nslindia.procurezone.dto.RoleCreateRequest;
import com.nslindia.procurezone.dto.RoleUpdateRequest;
import com.nslindia.procurezone.identity.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public ResponseEntity<Page<RoleDto>> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(roleService.getAllRoles(PageRequest.of(page, size)));
    }

    @GetMapping("/active")
    public ResponseEntity<List<RoleDto>> getActiveRoles() {
        return ResponseEntity.ok(roleService.getActiveRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable Integer id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @PostMapping
    public ResponseEntity<RoleDto> createRole(@RequestBody RoleCreateRequest request) {
        return ResponseEntity.ok(roleService.createRole(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleDto> updateRole(
            @PathVariable Integer id,
            @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(roleService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok().build();
    }
}
