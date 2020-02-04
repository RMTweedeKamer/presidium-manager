package nl.th8.presidium.archive.controller;

import nl.th8.presidium.archive.service.ArchiveService;
import nl.th8.presidium.scheduler.KamerstukNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/archive")
public class ArchiveController {

    @Autowired
    ArchiveService archiveService;

    @GetMapping(value = "/{type}/{id}", produces = "text/plain;charset=UTF-8")
    String getMotion(Model model, @PathVariable String type, @PathVariable String id) {
        try {
            model.addAttribute("content", archiveService.getKamerstukHtml(type, id));
            model.addAttribute("callsign", type + id);
            return "kamerstuk";
        } catch (KamerstukNotFoundException e) {
            return "redirect: /archive?notfound";
        }
    }
}
