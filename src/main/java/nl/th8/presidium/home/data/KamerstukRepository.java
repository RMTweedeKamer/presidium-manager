package nl.th8.presidium.home.data;

import nl.th8.presidium.home.controller.dto.Kamerstuk;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;

public interface KamerstukRepository extends MongoRepository<Kamerstuk, String> {

    List<Kamerstuk> findAllByPostDateIsNull();

    PriorityQueue<Kamerstuk> findAllByPostDateIsAfter(Date date);

    PriorityQueue<Kamerstuk> findAllByPostDateIsBefore(Date date);
}
