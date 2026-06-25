package com.nslindia.procurezone.notification.repository;

import com.nslindia.procurezone.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    Page<Notification> findByRecipientEmpNumberOrderByCreatedAtDesc(Integer empNumber, Pageable pageable);

    long countByRecipientEmpNumberAndReadFalse(Integer empNumber);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id = :id AND n.recipientEmpNumber = :empNumber")
    int markAsRead(@Param("id") Integer id, @Param("empNumber") Integer empNumber);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipientEmpNumber = :empNumber AND n.read = false")
    int markAllAsRead(@Param("empNumber") Integer empNumber);
}
