package nl.th8.presidium.user.data;

import nl.th8.presidium.user.controller.dto.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}
