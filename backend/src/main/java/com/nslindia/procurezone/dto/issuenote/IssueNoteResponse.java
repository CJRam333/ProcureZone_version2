package com.nslindia.procurezone.dto.issuenote;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueNoteResponse {
    private Integer id;
    private String issueNoteNumber;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime issueDate;

    private Integer companyId;
    private String companyCode;
    private String companyName;

    private Integer departmentId;
    private String departmentCode;
    private String departmentName;

    private Integer sectionId;
    private String sectionCode;
    private String sectionName;

    private Integer plantId;
    private String plantCode;
    private String plantName;

    private String issuedTo;
    private String purpose;
    private String comments;

    private Integer createdById;
    private String createdByName;
    private String createdByEmployeeNumber;

    private Integer approvedById;
    private String approvedByName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime approvedDate;

    private Integer storesByEmployeeId;
    private String storesByName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime storesDate;

    private Integer status; // 1=Draft, 2=Submitted, 3=Approved, 4=Rejected, 5=Issued
    private String statusText;
    private Integer approvedStatus; // 0=Pending, 1=Approved, 2=Rejected
    private Integer storesStatus; // 0=Pending, 1=Issued

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;
    private Integer lastModifiedBy;

    private List<IssueNoteDetailsDTO> details = new ArrayList<>();
}
