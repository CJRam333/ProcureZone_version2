package com.nslindia.procurezone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrgChartNode {

    private Integer employeeNumber;
    private Integer supervisorNumber;
    private int level;
    private List<OrgChartNode> subordinates = new ArrayList<>();

    public OrgChartNode(Integer employeeNumber, Integer supervisorNumber, int level) {
        this.employeeNumber = employeeNumber;
        this.supervisorNumber = supervisorNumber;
        this.level = level;
        this.subordinates = new ArrayList<>();
    }

    public void addSubordinate(OrgChartNode node) {
        this.subordinates.add(node);
    }

    public boolean hasSubordinates() {
        return subordinates != null && !subordinates.isEmpty();
    }

    public int getSubordinateCount() {
        return subordinates != null ? subordinates.size() : 0;
    }

    public int getTotalDescendantCount() {
        if (subordinates == null || subordinates.isEmpty()) {
            return 0;
        }
        int count = subordinates.size();
        for (OrgChartNode sub : subordinates) {
            count += sub.getTotalDescendantCount();
        }
        return count;
    }
}
