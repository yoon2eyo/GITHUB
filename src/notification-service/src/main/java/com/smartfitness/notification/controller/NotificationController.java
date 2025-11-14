package com.smartfitness.notification.controller;

import com.smartfitness.notification.service.INotificationDispatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Interface Layer: Notification Controller
 * Component: NotificationController
 * 
 * Manual notification dispatch API (for testing or manual triggers)
 * 
 * Reference: 06_NotificationDispatcherComponent.puml (INotificationApi)
 */
@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController implements INotificationApi {
    
    private final INotificationDispatcherService notificationDispatcherService;
    
    /**
     * Manual notification dispatch
     */
    @Override
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendNotification(
            @RequestParam String userId,
            @RequestParam String message) {
        
        log.info("Manual notification request: userId={}, message={}", userId, message);
        
        notificationDispatcherService.sendNotification(userId, message);
        
        return ResponseEntity.ok(Map.of(
                "status", "sent",
                "userId", userId
        ));
    }
}

