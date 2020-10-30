package nl.rmtk.presidium.scheduler.data;

import nl.rmtk.presidium.scheduler.controller.dto.Settings;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SettingsRepository extends MongoRepository<Settings, String> {

}
