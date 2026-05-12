package com.nslindia.procurezone.dto.issuenote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueNoteStatisticsDTO {
    private Long totalIssueNotes;
    private Long draftIssueNotes;
    private Long submittedIssueNotes;
    private Long approvedIssueNotes;
    private Long rejectedIssueNotes;
    private Long issuedIssueNotes;
    private Long pendingApproval;
    private Long pendingIssue;
}
