package nl.th8.presidium.scheduler.data;

import nl.th8.presidium.scheduler.controller.dto.Settings;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SettingsRepository extends MongoRepository<Settings, String> {

}
