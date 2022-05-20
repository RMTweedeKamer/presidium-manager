package nl.th8.presidium.scheduler.controller.dto;

import nl.th8.presidium.Constants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.aggregation.BooleanOperators;

import java.util.ArrayDeque;
import java.util.Deque;

@SuppressWarnings("unused")
public class Settings {

    public static final String SETTINGS_ID = "Settings";
    @Id
    private String id;
    private ArrayDeque<Notification> notifications;

    public Settings() {
        this.id = SETTINGS_ID;
        this.notifications = new ArrayDeque<>();
    }

    public Deque<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(ArrayDeque<Notification> notifications) {
        this.notifications = notifications;
    }

    public void addNotification(Notification notification) {
        this.notifications.push(notification);
        if(this.notifications.size() > Constants.MAX_NOTIFICATIONS) {
            this.notifications.removeLast();
        }
    }
}
