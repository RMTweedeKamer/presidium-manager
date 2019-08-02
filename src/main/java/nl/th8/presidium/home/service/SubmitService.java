package nl.th8.presidium.home.service;

import nl.th8.presidium.home.controller.dto.Kamerstuk;
import nl.th8.presidium.home.controller.dto.KamerstukType;
import nl.th8.presidium.home.data.KamerstukRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubmitService {

    @Autowired
    private KamerstukRepository kamerstukRepository;

    public void processKamerstuk(Kamerstuk kamerstuk) {
        kamerstuk.setType(KamerstukType.MOTIE); //TODO implement other types.
        kamerstukRepository.insert(kamerstuk);
    }
}
