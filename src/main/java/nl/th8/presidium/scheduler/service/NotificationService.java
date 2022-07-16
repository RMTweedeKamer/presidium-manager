package nl.th8.presidium.scheduler.service;

import nl.th8.presidium.Constants;
import nl.th8.presidium.scheduler.controller.dto.Notification;
import nl.th8.presidium.scheduler.controller.dto.Settings;
import nl.th8.presidium.scheduler.data.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;


@Service
public class NotificationService {

    private final SettingsProvider settingsProvider;

    @Autowired
    public NotificationService(SettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    public void addNotification(Notification notification) {
        ArrayDeque<Notification> notifications = settingsProvider.getNotifications();
        notifications.push(notification);
        if(notifications.size() > Constants.MAX_NOTIFICATIONS) {
            notifications.removeLast();
        }
        settingsProvider.setNotifications(notifications);
    }

    public Deque<Notification> getNotifications() {
        return settingsProvider.getNotifications();
    }
}
