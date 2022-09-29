package nl.th8.presidium.scheduler.service;

import nl.th8.presidium.scheduler.controller.dto.Notification;
import nl.th8.presidium.scheduler.controller.dto.Settings;
import nl.th8.presidium.scheduler.data.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

@Service
public class SettingsProvider {

    private final SettingsRepository settingsRepository;

    @Autowired
    public SettingsProvider(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public Settings getAllSettings() {
        Optional<Settings> settings = settingsRepository.findById(Settings.SETTINGS_ID);
        if(settings.isPresent()) {
            return settings.get();
        }
        else {
            Settings newSettings = new Settings();
            settingsRepository.save(newSettings);
            return newSettings;
        }
    }

    public ArrayDeque<Notification> getNotifications() {
        return getAllSettings().getNotifications();
    }

    public void setNotifications(ArrayDeque<Notification> notifications) {
        Settings settings = getAllSettings();
        settings.setNotifications(notifications);
        settingsRepository.save(settings);
    }

    public List<String> getTkMembers() {
        return getAllSettings().getTkUsers();
    }

    public void setTkMembers(String tkMembersString) {
        Settings settings = getAllSettings();
        settings.setTkUsersFromString(tkMembersString);
        settingsRepository.save(settings);
    }

    public void setTkMembers(List<String> tkMembersList) {
        Settings settings = getAllSettings();
        settings.setTkUsers(tkMembersList);
        settingsRepository.save(settings);
    }

    public String getTkMembersAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        getTkMembers().forEach(member -> {
            if(member.equals("NVT"))
                stringBuilder.append(member).append(System.lineSeparator());
            else
                stringBuilder.append("/u/").append(member).append(System.lineSeparator());
        });

        return stringBuilder.toString();
    }
}
