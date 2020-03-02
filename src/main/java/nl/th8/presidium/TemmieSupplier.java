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

@Service
public class TemmieSupplier {

    public String discordWebhookUrl;

    public TemmieWebhook discordWebhook;

    @Autowired
    public TemmieSupplier(@Value("${manager.discord-webhook-url}") String url) {
        this.discordWebhookUrl = url;

        discordWebhook = new TemmieWebhook(discordWebhookUrl);
    }

    public void defaultEmbeddedMessage(Kamerstuk kamerstuk) {
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

        discordWebhook.sendMessage(message);
    }
}
