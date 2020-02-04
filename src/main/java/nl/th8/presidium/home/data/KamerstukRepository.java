package nl.th8.presidium.home.data;

import nl.th8.presidium.home.controller.dto.Kamerstuk;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

public interface KamerstukRepository extends MongoRepository<Kamerstuk, String> {

    Optional<Kamerstuk> findByCallsignEqualsAndPostedIsTrue(String callsign);

    List<Kamerstuk> findAllByPostDateIsNull();

    PriorityQueue<Kamerstuk> findAllByPostDateIsAfterAndDeniedIsFalse(Date date);

    PriorityQueue<Kamerstuk> findAllByPostDateIsBeforeAndPostedIsFalseAndDeniedIsFalse(Date date);

    PriorityQueue<Kamerstuk> findAllByVoteDateAfterAndPostedIsTrue(Date date);

    PriorityQueue<Kamerstuk> findAllByPostedIsTrueAndVotePostedIsFalse();

    List<Kamerstuk> findAllByPostDateIsNullAndDeniedIsFalse();

    long countAllByPostDateIsNullAndDeniedIsFalse();

    long countAllByPostDateIsNotNullAndPostedIsFalse();

    long countAllByPostedIsTrueAndVotePostedIsFalse();

    long countAllByDeniedIsTrue();
}
