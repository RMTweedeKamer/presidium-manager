package nl.th8.presidium.home.data;

import nl.th8.presidium.home.controller.dto.Kamerstuk;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KamerstukRepository extends MongoRepository<Kamerstuk, String> {
}
