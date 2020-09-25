package nl.th8.presidium.scheduler.controller.dto;

import nl.th8.presidium.Constants;

import java.util.ArrayDeque;

@SuppressWarnings("unused")
public class Settings {

    private ArrayDeque<Notification> notifications;

    public Settings() {
        this.notifications = new ArrayDeque<>();
    }

    public ArrayDeque<Notification> getNotifications() {
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
