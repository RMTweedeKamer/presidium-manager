package nl.th8.presidium;

import nl.th8.presidium.home.controller.dto.StatDTO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Date;

@AutoConfigureBefore
@SpringBootApplication
@EnableScheduling
public class App {
    public static void main(String[] args) {
        StatDTO.setStartDate(new Date());
        SpringApplication.run(App.class, args);
    }

}