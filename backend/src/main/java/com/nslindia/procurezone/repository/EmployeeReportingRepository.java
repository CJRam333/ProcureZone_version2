package com.nslindia.procurezone.repository;

import com.nslindia.procurezone.entity.EmployeeReporting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeReportingRepository extends JpaRepository<EmployeeReporting, Integer> {

    Page<EmployeeReporting> findByStatus(Integer status, Pageable pageable);

    @Query("SELECT er FROM EmployeeReporting er WHERE er.status = 1")
    Page<EmployeeReporting> findAllActive(Pageable pageable);

    // Find all subordinates reporting to a supervisor
    @Query("SELECT er FROM EmployeeReporting er WHERE er.supervisorEmployeeNumber = :supervisorId AND er.status = 1")
    List<EmployeeReporting> findActiveSubordinates(@Param("supervisorId") Integer supervisorId);

    // Active supervisor ROWS for a subordinate — List (never throws on a stray duplicate),
    // deterministically ordered (latest report_start, then latest id first).
    @Query("SELECT er FROM EmployeeReporting er " +
            "WHERE er.subordinateEmployeeNumber = :subordinateId AND er.status = 1 " +
            "ORDER BY er.reportStart DESC, er.id DESC")
    List<EmployeeReporting> findActiveSupervisorRows(@Param("subordinateId") Integer subordinateId);

    /**
     * Enforce single-supervisor: deactivate ALL currently-active reporting rows for a subordinate
     * (soft-delete, keeping history) before a new one is inserted. Shared by every assignment path.
     */
    default void deactivateActiveSupervisors(Integer subordinateId) {
        java.time.LocalDate today = java.time.LocalDate.now();
        for (EmployeeReporting r : findActiveSupervisorRows(subordinateId)) {
            r.setStatus(0);
            r.setEndDate(today);
            r.setLastModifiedDate(today);
            save(r);
        }
    }

    // Get all subordinate employee numbers for a supervisor
    @Query("SELECT er.subordinateEmployeeNumber FROM EmployeeReporting er " +
            "WHERE er.supervisorEmployeeNumber = :supervisorId AND er.status = 1")
    List<Integer> findSubordinateNumbers(@Param("supervisorId") Integer supervisorId);

    // Active supervisor employee number(s) for a subordinate — List so it NEVER throws on a stray
    // duplicate; callers take the first. Deterministic order (latest report_start, then latest id).
    @Query("SELECT er.supervisorEmployeeNumber FROM EmployeeReporting er " +
            "WHERE er.subordinateEmployeeNumber = :subordinateId AND er.status = 1 " +
            "ORDER BY er.reportStart DESC, er.id DESC")
    List<Integer> findActiveSupervisors(@Param("subordinateId") Integer subordinateId);

    // Check if a reporting relationship exists
    boolean existsBySubordinateEmployeeNumberAndSupervisorEmployeeNumberAndStatus(
            Integer subordinateId, Integer supervisorId, Integer status);

    // Find by subordinate
    Page<EmployeeReporting> findBySubordinateEmployeeNumber(Integer subordinateId, Pageable pageable);

    // Find by supervisor
    Page<EmployeeReporting> findBySupervisorEmployeeNumber(Integer supervisorId, Pageable pageable);

    // Count subordinates
    @Query("SELECT COUNT(er) FROM EmployeeReporting er " +
            "WHERE er.supervisorEmployeeNumber = :supervisorId AND er.status = 1")
    long countActiveSubordinates(@Param("supervisorId") Integer supervisorId);

    // Check if employee has supervisor
    @Query("SELECT CASE WHEN COUNT(er) > 0 THEN true ELSE false END FROM EmployeeReporting er " +
            "WHERE er.subordinateEmployeeNumber = :employeeId AND er.status = 1")
    boolean hasSupervisor(@Param("employeeId") Integer employeeId);

    // Check if employee is supervisor
    @Query("SELECT CASE WHEN COUNT(er) > 0 THEN true ELSE false END FROM EmployeeReporting er " +
            "WHERE er.supervisorEmployeeNumber = :employeeId AND er.status = 1")
    boolean isSupervisor(@Param("employeeId") Integer employeeId);

    // Find all reporting relationships for an employee (as subordinate or
    // supervisor)
    @Query("SELECT er FROM EmployeeReporting er " +
            "WHERE (er.subordinateEmployeeNumber = :employeeId OR er.supervisorEmployeeNumber = :employeeId) " +
            "AND er.status = 1")
    List<EmployeeReporting> findAllRelationships(@Param("employeeId") Integer employeeId);
}
