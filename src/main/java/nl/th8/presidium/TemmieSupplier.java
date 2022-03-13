package nl.th8.presidium;

import com.mrpowergamerbr.temmiewebhook.DiscordEmbed;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;
import com.mrpowergamerbr.temmiewebhook.embed.FieldEmbed;
import net.dean.jraw.ApiException;
import nl.th8.presidium.home.controller.dto.Kamerstuk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class TemmieSupplier {

    private final TemmieWebhook schedulerWebhook;
    private final TemmieWebhook complaintWebhook;
    private final TemmieWebhook rvsWebhook;

    @Autowired
    public TemmieSupplier(@Value("${manager.discord-scheduler-webhook}") String schedulerUrl,
                          @Value("${manager.discord-complaint-webhook}") String complaintUrl,
                          @Value("${manager.discord-rvs-webhook}") String rvsUrl) {
        schedulerWebhook = new TemmieWebhook(schedulerUrl);
        complaintWebhook = new TemmieWebhook(complaintUrl);
        rvsWebhook = new TemmieWebhook(rvsUrl);
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
                .url("https://indienen.rmtk.nl/scheduler")
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

    public void schedulerErrorEmbeddedMessage(ApiException exception, Kamerstuk kamerstuk) {
        int color = 13980228;

        DiscordEmbed embed = DiscordEmbed.builder()
                .title("Er ging iets mis met het posten van " + kamerstuk.getCallsign())
                .description("Het kamerstuk is teruggezet naar de wachtrij. De error was: " + exception.getMessage() + ", als je dit niet snapt, laat het weten aan th8.")
                .url("https://indienen.rmtk.nl/scheduler")
                .color(color)
                .build();

        DiscordMessage message = DiscordMessage.builder()
                .username("GERDI-RMTK")
                .content("")
                .embed(embed)
                .build();

        schedulerWebhook.sendMessage(message);
    }

    public void rvsEmbeddedMessage(Kamerstuk kamerstuk) {
        int color = 8240616;

        DiscordEmbed embed = DiscordEmbed.builder()
                .title(kamerstuk.getTitle())
                .description("Er is een nieuw kamerstuk ter beoordeling voor de Raad van State.")
                .url("https://indienen.rmtk.nl/rvs")
                .fields(Arrays.asList(
                        FieldEmbed.builder()
                                .name("Type")
                                .value(kamerstuk.getType().getName())
                                .build(),
                        FieldEmbed.builder()
                                .name("Indiener")
                                .value(kamerstuk.getSubmittedBy())
                                .build(),
                        FieldEmbed.builder()
                                .name("Link")
                                .value("[Ga naar de RvS tool](https://indienen.th8.nl/rvs)")
                                .build()
                ))
                .color(color)
                .build();

        DiscordMessage message = DiscordMessage.builder()
                .username("GERDI-RMTK")
                .content("")
                .embed(embed)
                .build();

        rvsWebhook.sendMessage(message);
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
