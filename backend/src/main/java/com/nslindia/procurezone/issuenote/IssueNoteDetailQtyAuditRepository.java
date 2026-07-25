package com.nslindia.procurezone.issuenote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for issue-note line-quantity audit rows.
 */
@Repository
public interface IssueNoteDetailQtyAuditRepository extends JpaRepository<IssueNoteDetailQtyAudit, Integer> {

    /**
     * All audit rows for the given detail ids, oldest edit first.
     */
    List<IssueNoteDetailQtyAudit> findByDetailIdInOrderByEditedAtAsc(List<Integer> detailIds);
}
