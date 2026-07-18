package com.example.mccms.service;

import com.example.mccms.dto.NotificationResponse;
import com.example.mccms.model.Deliverable;
import com.example.mccms.model.Notification;
import com.example.mccms.model.User;
import com.example.mccms.repository.NotificationRepository;
import com.example.mccms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createNotification(User user, Deliverable deliverable, String type, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setRelatedDeliverable(deliverable);
        notification.setType(type);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    public List<NotificationResponse> getNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getType(),
                        n.getMessage(),
                        n.isRead(),
                        n.getCreatedAt()
                )).collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public long getUnreadCount(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
}
