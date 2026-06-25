package com.nslindia.procurezone.issuenote.dto;

import java.util.List;

public record IssueNoteFormMetaResponse(
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
        String nextIssueNoteNumber
) {
    public record CompanyInfo(Integer id, String name) {}
}
