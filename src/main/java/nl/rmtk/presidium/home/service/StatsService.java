package nl.rmtk.presidium.home.service;

import nl.rmtk.presidium.home.controller.dto.StatDTO;
import nl.rmtk.presidium.home.data.KamerstukRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    private final KamerstukRepository repository;

    @Autowired
    public StatsService(KamerstukRepository repository) {
        this.repository = repository;
    }

    public StatDTO getStats() {
        long kamerstukken = repository.count();
        long queue = repository.countAllByPostDateIsNullAndDeniedIsFalse() + repository.countAllByPostDateIsNotNullAndPostedIsFalse();
        long queueVote = repository.countAllByPostedIsTrueAndVotePostedIsFalseAndDeniedIsFalse();
        long denied = repository.countAllByPostedIsFalseAndDeniedIsTrue();
        long withdrawn = repository.countAllByPostedIsTrueAndDeniedIsTrue();
        long succesfullyPosted = repository.countAllByPostedIsTrueAndVotePostedIsTrue();

        return new StatDTO(kamerstukken, queue, queueVote, denied, withdrawn, succesfullyPosted);
    }
}
