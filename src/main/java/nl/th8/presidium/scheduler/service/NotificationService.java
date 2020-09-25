package nl.th8.presidium.scheduler.service;

import nl.th8.presidium.scheduler.controller.dto.Notification;
import nl.th8.presidium.scheduler.controller.dto.Settings;
import nl.th8.presidium.scheduler.data.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class NotificationService {

    private final SettingsRepository settingsRepository;

    @Autowired
    public NotificationService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public Settings getAllSettings() {
        Optional<Settings> settings = settingsRepository.findById("Settings");
        if(settings.isPresent()) {
            return settings.get();
        }
        else {
            Settings newSettings = new Settings();
            settingsRepository.save(newSettings);
            return newSettings;
        }
    }

    public void addNotification(Notification notification) {
        Settings settings = getAllSettings();
        settings.addNotification(notification);
        settingsRepository.save(settings);
    }


}
