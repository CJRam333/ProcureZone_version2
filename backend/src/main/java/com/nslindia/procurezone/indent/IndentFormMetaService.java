package com.nslindia.procurezone.indent;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.identity.EmployeeRepository;
import com.nslindia.procurezone.indent.dto.IndentFormMetaResponse;
import com.nslindia.procurezone.issuenote.IssueNoteService;
import com.nslindia.procurezone.issuenote.dto.IssueNoteFormMetaResponse;
import com.nslindia.procurezone.masterdata.LocationRepository;
import com.nslindia.procurezone.masterdata.repository.CompanyRepository;
import com.nslindia.procurezone.masterdata.repository.DepartmentRepository;
import com.nslindia.procurezone.repository.CompanyEmployeeRepository;
import com.nslindia.procurezone.security.UserPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndentFormMetaService {

    private final EmployeeRepository employeeRepository;
    private final CompanyEmployeeRepository companyEmployeeRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final LocationRepository locationRepository;
    private final IndentService indentService;
    private final IssueNoteService issueNoteService;

    @Transactional(readOnly = true)
    public IndentFormMetaResponse getIndentMeta(UserPrincipal principal) {
        Employee emp = employeeRepository.findByEmployeeNumber(principal.employeeNumber())
                .orElseThrow(() -> new RuntimeException("Employee not found: " + principal.employeeNumber()));

        List<Integer> companyIds = companyEmployeeRepository.findCompanyIdsByEmpNumber(principal.employeeNumber());
        List<IndentFormMetaResponse.CompanyInfo> companies = companyRepository.findAllById(companyIds)
                .stream()
                .map(c -> new IndentFormMetaResponse.CompanyInfo(c.getId(), c.getName()))
                .collect(Collectors.toList());
        Integer defaultCompanyId = companies.isEmpty() ? null : companies.get(0).id();

        Integer deptId = emp.getDepartmentId();
        String deptName = deptId != null
                ? departmentRepository.findById(deptId).map(d -> d.getName()).orElse(null)
                : null;

        Integer locId = emp.getLocationId();
        String locName = locId != null
                ? locationRepository.findById(locId).map(l -> l.getName()).orElse(null)
                : null;

        return new IndentFormMetaResponse(
                emp.getEmployeeNumber(),
                emp.getFullName(),
                emp.getEmployeeId(),
                deptId,
                deptName,
                companies,
                defaultCompanyId,
                locId,
                locName,
                IndentService.getCurrentFinancialYear(),
                LocalDate.now().toString(),
                indentService.previewNextIndentNumber()
        );
    }

    @Transactional(readOnly = true)
    public IssueNoteFormMetaResponse getIssueNoteMeta(UserPrincipal principal) {
        Employee emp = employeeRepository.findByEmployeeNumber(principal.employeeNumber())
                .orElseThrow(() -> new RuntimeException("Employee not found: " + principal.employeeNumber()));

        List<Integer> companyIds = companyEmployeeRepository.findCompanyIdsByEmpNumber(principal.employeeNumber());
        List<IssueNoteFormMetaResponse.CompanyInfo> companies = companyRepository.findAllById(companyIds)
                .stream()
                .map(c -> new IssueNoteFormMetaResponse.CompanyInfo(c.getId(), c.getName()))
                .collect(Collectors.toList());
        Integer defaultCompanyId = companies.isEmpty() ? null : companies.get(0).id();

        Integer deptId = emp.getDepartmentId();
        String deptName = deptId != null
                ? departmentRepository.findById(deptId).map(d -> d.getName()).orElse(null)
                : null;

        Integer locId = emp.getLocationId();
        String locName = locId != null
                ? locationRepository.findById(locId).map(l -> l.getName()).orElse(null)
                : null;

        return new IssueNoteFormMetaResponse(
                emp.getEmployeeNumber(),
                emp.getFullName(),
                emp.getEmployeeId(),
                deptId,
                deptName,
                companies,
                defaultCompanyId,
                locId,
                locName,
                IndentService.getCurrentFinancialYear(),
                LocalDate.now().toString(),
                issueNoteService.previewNextIssueNoteNumber()
        );
    }
}
