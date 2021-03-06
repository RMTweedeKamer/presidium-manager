package nl.th8.presidium.home.service;

import nl.th8.presidium.TemmieSupplier;
import nl.th8.presidium.home.controller.dto.Kamerstuk;
import nl.th8.presidium.home.data.KamerstukRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubmitService {

    private final KamerstukRepository kamerstukRepository;

    private final TemmieSupplier discordSupplier;

    @Autowired
    public SubmitService(KamerstukRepository kamerstukRepository, TemmieSupplier discordSupplier) {
        this.kamerstukRepository = kamerstukRepository;
        this.discordSupplier = discordSupplier;
    }

    public void processKamerstuk(Kamerstuk kamerstuk) throws IllegalArgumentException {
        if (kamerstuk.getTitle().isEmpty() ||
            kamerstuk.getContent().isEmpty() ||
            kamerstuk.getSubmittedBy().isEmpty()) {
            throw new IllegalArgumentException();
        }
        kamerstukRepository.insert(kamerstuk);

        discordSupplier.schedulerEmbeddedMessage(kamerstuk);
    }
}
