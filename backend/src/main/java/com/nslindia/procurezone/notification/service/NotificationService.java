package com.nslindia.procurezone.notification.service;

import com.nslindia.procurezone.notification.model.Notification;
import com.nslindia.procurezone.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(Integer recipientEmpNumber, String type,
                                   String title, String message,
                                   String entityType, Integer entityId, String link) {
        try {
            Notification notif = Notification.builder()
                    .recipientEmpNumber(recipientEmpNumber)
                    .type(type)
                    .title(title)
                    .message(message)
                    .entityType(entityType)
                    .entityId(entityId)
                    .link(link)
                    .build();
            notificationRepository.save(notif);
        } catch (Exception e) {
            log.error("Failed to create notification for emp {}: {}", recipientEmpNumber, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<Notification> getNotifications(Integer empNumber, int page, int size) {
        return notificationRepository.findByRecipientEmpNumberOrderByCreatedAtDesc(
                empNumber, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Integer empNumber) {
        return notificationRepository.countByRecipientEmpNumberAndReadFalse(empNumber);
    }

    @Transactional
    public boolean markAsRead(Integer notifId, Integer empNumber) {
        return notificationRepository.markAsRead(notifId, empNumber) > 0;
    }

    @Transactional
    public int markAllAsRead(Integer empNumber) {
        return notificationRepository.markAllAsRead(empNumber);
    }
}
