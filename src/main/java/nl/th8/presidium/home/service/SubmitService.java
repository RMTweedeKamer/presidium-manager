package nl.th8.presidium.home.service;

import nl.th8.presidium.TemmieSupplier;
import nl.th8.presidium.home.controller.dto.Kamerstuk;
import nl.th8.presidium.home.controller.dto.KamerstukType;
import nl.th8.presidium.home.data.KamerstukRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubmitService {

    @Autowired
    private KamerstukRepository kamerstukRepository;

    @Autowired
    private TemmieSupplier discordSupplier;

    public void processKamerstuk(Kamerstuk kamerstuk) throws IllegalArgumentException {
        if (kamerstuk.getTitle().isEmpty() ||
            kamerstuk.getContent().isEmpty() ||
            kamerstuk.getSubmittedBy().isEmpty()) {
            throw new IllegalArgumentException();
        }
        kamerstukRepository.insert(kamerstuk);

        discordSupplier.defaultEmbeddedMessage(kamerstuk);
    }
}
