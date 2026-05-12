package com.nslindia.procurezone.identity;

import com.nslindia.procurezone.dto.RoleDto;
import com.nslindia.procurezone.dto.RoleCreateRequest;
import com.nslindia.procurezone.dto.RoleUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public Page<RoleDto> getAllRoles(@NonNull Pageable pageable) {
        Page<Role> roles = roleRepository.findAll(pageable);
        List<RoleDto> dtos = roles.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, roles.getTotalElements());
    }

    public List<RoleDto> getActiveRoles() {
        return roleRepository.findAll().stream()
                .filter(role -> role.isActive())
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public RoleDto getRoleById(@NonNull Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return mapToDto(role);
    }

    public RoleDto createRole(RoleCreateRequest request) {
        if (roleRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Role with code " + request.getCode() + " already exists");
        }

        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        role.setCanView(request.isCanView() ? "1" : "0");
        role.setCanAdd(request.isCanAdd() ? "1" : "0");
        role.setCanEdit(request.isCanEdit() ? "1" : "0");
        role.setCanDelete(request.isCanDelete() ? "1" : "0");
        
        // Audit fields
        role.setLastModifiedDate(LocalDate.now());
        role.setLastModifiedUser(1); // System or default user

        Role saved = roleRepository.save(role);
        return mapToDto(saved);
    }

    public RoleDto updateRole(@NonNull Integer id, RoleUpdateRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (request.getCode() != null && !request.getCode().equals(role.getCode())) {
            if (roleRepository.existsByCode(request.getCode())) {
                throw new RuntimeException("Role with code " + request.getCode() + " already exists");
            }
            role.setCode(request.getCode());
        }

        if (request.getName() != null) {
            role.setName(request.getName());
        }
        
        if (request.getStatus() != null) {
            role.setStatus(request.getStatus());
        }

        // The frontend always sends the full UI boolean state for checkboxes during update
        role.setCanView(request.isCanView() ? "1" : "0");
        role.setCanAdd(request.isCanAdd() ? "1" : "0");
        role.setCanEdit(request.isCanEdit() ? "1" : "0");
        role.setCanDelete(request.isCanDelete() ? "1" : "0");

        role.setLastModifiedDate(LocalDate.now());

        Role updated = roleRepository.save(role);
        return mapToDto(updated);
    }

    public void deleteRole(@NonNull Integer id) {
        roleRepository.deleteById(id);
    }

    private RoleDto mapToDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setName(role.getName());
        dto.setStatus(role.getStatus());
        
        // Null or "1" maps to true (depending on how the DB treats superadmin), "0" defaults to false.
        // Assuming NULL means they didn't have explicit restriction or it's an old record.
        dto.setCanView("1".equals(role.getCanView()) || "true".equalsIgnoreCase(role.getCanView()));
        dto.setCanAdd("1".equals(role.getCanAdd()) || "true".equalsIgnoreCase(role.getCanAdd()));
        dto.setCanEdit("1".equals(role.getCanEdit()) || "true".equalsIgnoreCase(role.getCanEdit()));
        dto.setCanDelete("1".equals(role.getCanDelete()) || "true".equalsIgnoreCase(role.getCanDelete()));
        
        dto.setCreatedAt(role.getLastModifiedDate());
        dto.setUpdatedAt(role.getLastModifiedDate());
        return dto;
    }
}
