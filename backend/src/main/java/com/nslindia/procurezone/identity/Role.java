package com.nslindia.procurezone.identity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
@Entity
@Table(name = "tbl_roles_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer id;

    @Column(name = "role_code", length = 100)
    private String code;

    @Column(name = "role_name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "role_view", length = 100)
    private String canView;
    
    @Column(name = "role_add", length = 100)
    private String canAdd;
    
    @Column(name = "role_edit", length = 100)
    private String canEdit;
    
    @Column(name = "role_delete", length = 100)
    private String canDelete;

    @Column(name = "role_status", nullable = false)
    private Integer status;

    @Column(name = "role_lmd", nullable = false)
    private LocalDate lastModifiedDate;

    @Column(name = "role_lmu", nullable = false)
    private Integer lastModifiedUser;

    public boolean isActive() {
        return Objects.equals(status, 1);
    }
}
