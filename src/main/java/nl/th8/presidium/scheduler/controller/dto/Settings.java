package nl.th8.presidium.scheduler.controller.dto;

import nl.th8.presidium.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.aggregation.BooleanOperators;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@SuppressWarnings("unused")
public class Settings {

    public static final String SETTINGS_ID = "Settings";
    @Id
    private String id;
    private ArrayDeque<Notification> notifications;

    //List of Reddit usernames with /u/, empty lines used to skip a line for use in spreadsheet.
    private List<String> tkUsers;

    public Settings() {
        this.id = SETTINGS_ID;
        this.notifications = new ArrayDeque<>();
        this.tkUsers = new ArrayList<>();
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

    public List<String> getTkUsers() {
        return tkUsers;
    }

    public void setTkUsers(List<String> tkUsers) {
        this.tkUsers = tkUsers;
    }

    public void setTkUsersFromString(String tkUsersString) {
        this.tkUsers.clear();

        String[] split = tkUsersString.split("\\r?\\n");
        for(String tkMember : split) {
            //If is valid reddit username or empty line
            if(tkMember.matches("/u/[A-Za-z0-9_-]+"))
                this.tkUsers.add(tkMember.substring(3));
            else if(tkMember.equals("NVT"))
                this.tkUsers.add("NVT");
            else if(StringUtils.isBlank(tkMember))
                this.tkUsers.add("");
        }
    }
}
