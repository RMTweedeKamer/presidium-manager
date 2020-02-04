package nl.th8.presidium.archive.service;

import nl.th8.presidium.home.controller.dto.Kamerstuk;
import nl.th8.presidium.home.data.KamerstukRepository;
import nl.th8.presidium.scheduler.KamerstukNotFoundException;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ArchiveService {

    @Autowired
    KamerstukRepository repository;

    public String getKamerstukHtml(String type, String id) throws KamerstukNotFoundException {
        if(id.length() < 4) {
            id = padLeftZeros(id, 4);
        }
        type = type.toUpperCase();

        System.out.println(type+id);

        Optional<Kamerstuk> possibleKamerstuk = repository.findByCallsignEqualsAndPostedIsTrue(type+id);

        if(possibleKamerstuk.isPresent()) {
            MutableDataSet options = new MutableDataSet();
            Kamerstuk kamerstuk = possibleKamerstuk.get();
            options.set(Parser.HEADING_NO_ATX_SPACE, true)
                    .set(Parser.LISTS_CODE_INDENT, 1)
                    .set(Parser.LISTS_ITEM_INDENT, 1);
            String kamerstukText = String.format("##%s \n \n%s", kamerstuk.getTitle(), kamerstuk.getContent());
            Parser parser = Parser.builder(options).build();
            Node kamerstukMarkdown = parser.parse(kamerstukText);
            HtmlRenderer renderer = HtmlRenderer.builder(options).build();

            return renderer.render(kamerstukMarkdown);
        }
        throw new KamerstukNotFoundException();
    }

    private String padLeftZeros(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }
        sb.append(inputString);

        return sb.toString();
    }
}
