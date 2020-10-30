package nl.rmtk.presidium;

import nl.rmtk.presidium.home.controller.dto.StatDTO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Date;
import java.util.TimeZone;

@AutoConfigureBefore
@SpringBootApplication
@EnableScheduling
public class App {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Amsterdam"));
        StatDTO.setStartDate(new Date());
        SpringApplication.run(App.class, args);
    }

}
