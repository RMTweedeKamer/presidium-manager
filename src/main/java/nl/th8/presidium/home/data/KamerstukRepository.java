package nl.th8.presidium.home.data;

import nl.th8.presidium.home.controller.dto.Kamerstuk;
import nl.th8.presidium.home.controller.dto.KamerstukType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

public interface KamerstukRepository extends MongoRepository<Kamerstuk, String> {

    Optional<Kamerstuk> findByCallsignEqualsAndPostedIsTrue(String callsign);

    List<Kamerstuk> findAllByPostDateIsAfterAndDeniedIsFalseAndPostedIsFalse(Date date);

    PriorityQueue<Kamerstuk> findAllByPostDateIsBeforeAndPostedIsFalseAndDeniedIsFalse(Date date);

    List<Kamerstuk> findAllByPostedIsTrueAndVotePostedIsFalseAndDeniedIsFalseAndVoteDateIsNotNull();

    List<Kamerstuk> findAllByPostedIsTrueAndVotePostedIsFalseAndDeniedIsFalseAndVoteDateIsNull();

    List<Kamerstuk> findAllByPostedIsTrueAndCallsignContains(String contains);

    List<Kamerstuk> findAllByPostDateIsNullAndTypeIsNotAndDeniedIsFalse(KamerstukType type);

    List<Kamerstuk> findAllByTypeIsAndDeniedIsFalseAndVoteProcessedIsFalseAndPostDateIsNull(KamerstukType type);

    List<Kamerstuk> findAllByPostDateIsBetweenAndCallsignIsNotNull(Date date1, Date date2);

    List<Kamerstuk> findAllByPostDateIsBeforeAndCallsignIsNotNull(Date date1);

    List<Kamerstuk> findAllByDeniedIsTrueAndPostedIsFalse();

    List<Kamerstuk> findAllByTypeEqualsAndPostDateNotNullAndPostedIsFalse(KamerstukType type);

    List<Kamerstuk> findAllByTypeEqualsAndPostDateBeforeAndPostedIsFalseAndBundleTitleIsNotNull(KamerstukType type, Date postDateBefore);

    List<Kamerstuk> findAllByTypeEqualsAndVoteDateBeforeAndPostedIsTrueAndDeniedIsFalseAndVoteProcessedIsFalse(KamerstukType type, Date voteDateBefore);

    Optional<Kamerstuk> findByUrlContainsOrUrlContains(String contains1, String contains2);

    long countAllByPostDateIsNullAndDeniedIsFalse();

    long countAllByPostDateIsAfterAndDeniedIsFalseAndPostedIsFalse(Date date);

    long countAllByPostedIsTrueAndVotePostedIsFalseAndDeniedIsFalseAndVoteDateIsNotNull();

    long countAllByPostedIsFalseAndDeniedIsTrue();

    long countAllByPostedIsTrueAndDeniedIsTrue();

    long countAllByPostedIsTrueAndVotePostedIsTrue();

    long countAllByPostedIsTrueAndVotePostedIsFalseAndVoteDateIsNullAndDeniedIsFalse();

    boolean existsByCallsignAndIdIsNot(String callsign, String id);

    boolean existsByCallsign(String callsign);
}
