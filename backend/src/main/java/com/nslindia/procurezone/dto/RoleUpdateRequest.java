package com.nslindia.procurezone.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateRequest {
    private String code;
    private String name;
    private boolean canView;
    private boolean canAdd;
    private boolean canEdit;
    private boolean canDelete;
    private Integer status;
}
