package com.nslindia.procurezone.moduleaccess;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleMasterRepository extends JpaRepository<ModuleMaster, String> {

    @Query("SELECT m FROM ModuleMaster m ORDER BY m.isFuture ASC, m.moduleCode ASC")
    List<ModuleMaster> findAllOrdered();
}
