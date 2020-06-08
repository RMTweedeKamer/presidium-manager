package nl.th8.presidium;

import com.mrpowergamerbr.temmiewebhook.DiscordEmbed;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;
import com.mrpowergamerbr.temmiewebhook.embed.FieldEmbed;
import nl.th8.presidium.home.controller.dto.Kamerstuk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;

@Service
public class TemmieSupplier {

    private TemmieWebhook schedulerWebhook;
    private TemmieWebhook complaintWebhook;

    @Autowired
    public TemmieSupplier(@Value("${manager.discord-scheduler-webhook}") String schedulerUrl, @Value("${manager.discord-complaint-webhook}") String complaintUrl) {
        schedulerWebhook = new TemmieWebhook(schedulerUrl);
        complaintWebhook = new TemmieWebhook(complaintUrl);
    }

    public void schedulerEmbeddedMessage(Kamerstuk kamerstuk) {
        int color;
        if(kamerstuk.isUrgent())
            color = 13980228;
        else
            color = 8240616;

        DiscordEmbed embed = DiscordEmbed.builder()
                .title(kamerstuk.getTitle())
                .description("Er is een nieuw kamerstuk ingediend in het indienpaneel.")
                .url("https://indienen.th8.nl/scheduler")
                .fields(Arrays.asList(
                        FieldEmbed.builder()
                                .name("Type")
                                .value(kamerstuk.getType().getName())
                                .build(),
                        FieldEmbed.builder()
                                .name("Indiener")
                                .value(kamerstuk.getSubmittedBy())
                                .build()
                ))
                .color(color)
                .build();

        DiscordMessage message = DiscordMessage.builder()
                .username("GERDI-RMTK")
                .content("")
                .embed(embed)
                .build();

        schedulerWebhook.sendMessage(message);
    }

    public void complaintEmbeddedMessage(String complaint, String url) {
        int color = 13980228;

        DiscordEmbed embed = DiscordEmbed.builder()
                .title("Er is een nieuwe klacht ingediend")
                .url(url)
                .fields(Arrays.asList(
                        FieldEmbed.builder()
                                .name("Klacht")
                                .value(complaint)
                                .build(),
                        FieldEmbed.builder()
                            .name("Link")
                            .value("[Ga naar het bericht]("+url+")")
                            .build()
                ))
                .color(color)
                .build();

        DiscordMessage message = DiscordMessage.builder()
                .username("GERDI-RMTK")
                .content("<@&719565937497604236>")
                .embed(embed)
                .build();

        complaintWebhook.sendMessage(message);
    }
}
