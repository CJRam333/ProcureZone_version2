package com.nslindia.procurezone.identity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    @Query("""
                SELECT DISTINCT u FROM UserAccount u
                JOIN FETCH u.employee e
                LEFT JOIN FETCH e.employeeRoles er
                LEFT JOIN FETCH er.role r
                WHERE LOWER(u.username) = LOWER(:username)
            """)
    Optional<UserAccount> findUserForLogin(@Param("username") String username);
}
