package com.nslindia.procurezone.indent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for indent line-quantity audit rows.
 */
@Repository
public interface IndentDetailQtyAuditRepository extends JpaRepository<IndentDetailQtyAudit, Integer> {

    /**
     * All audit rows for the given detail ids, oldest edit first — used to build each line's
     * quantity history in one query per document.
     */
    List<IndentDetailQtyAudit> findByDetailIdInOrderByEditedAtAsc(List<Integer> detailIds);
}
