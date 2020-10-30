package nl.rmtk.presidium.user.data;

import nl.rmtk.presidium.user.controller.dto.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}
