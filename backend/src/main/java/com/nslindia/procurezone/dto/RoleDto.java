package com.nslindia.procurezone.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
    private Integer id;
    private String code;
    private String name;
    private boolean canView;
    private boolean canAdd;
    private boolean canEdit;
    private boolean canDelete;
    private Integer status;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
