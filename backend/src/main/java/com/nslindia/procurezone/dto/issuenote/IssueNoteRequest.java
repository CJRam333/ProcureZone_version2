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
public class IssueNoteRequest {
    private Integer id; // Only for updates

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime issueDate;

    private Integer companyId;
    private Integer departmentId;
    private Integer sectionId;
    private Integer plantId;
    private String issuedTo;
    private String purpose;
    private String comments;
    private Integer status; // 1=Draft, 2=Submitted, 3=Approved, 4=Rejected, 5=Issued

    private List<IssueNoteDetailsDTO> details = new ArrayList<>();
}
