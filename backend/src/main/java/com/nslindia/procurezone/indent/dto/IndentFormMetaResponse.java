package com.nslindia.procurezone.indent.dto;

import java.util.List;

public record IndentFormMetaResponse(
        Integer empNumber,
        String empName,
        String empId,
        Integer departmentId,
        String departmentName,
        List<CompanyInfo> companies,
        Integer defaultCompanyId,
        Integer locationId,
        String locationName,
        String financialYear,
        String date,
        String nextIndentNumber
) {
    public record CompanyInfo(Integer id, String name) {}
}
