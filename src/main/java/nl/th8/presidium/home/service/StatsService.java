package nl.th8.presidium.home.service;

import nl.th8.presidium.home.controller.dto.StatDTO;
import nl.th8.presidium.home.data.KamerstukRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class StatsService {

    private final KamerstukRepository repository;

    @Autowired
    public StatsService(KamerstukRepository repository) {
        this.repository = repository;
    }

    public StatDTO getStats() {
        long kamerstukken = repository.count();
        long queue = repository.countAllByPostDateIsNullAndDeniedIsFalse() + repository.countAllByPostDateIsAfterAndDeniedIsFalseAndPostedIsFalse(new Date());
        long queueVote = repository.countAllByPostedIsTrueAndVotePostedIsFalseAndDeniedIsFalseAndVoteDateIsNotNull();
        long denied = repository.countAllByPostedIsFalseAndDeniedIsTrue();
        long withdrawn = repository.countAllByPostedIsTrueAndDeniedIsTrue();
        long succesfullyPosted = repository.countAllByPostedIsTrueAndVotePostedIsTrue();

        return new StatDTO(kamerstukken, queue, queueVote, denied, withdrawn, succesfullyPosted);
    }
}
