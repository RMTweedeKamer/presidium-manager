package nl.th8.presidium.archive.controller;

import nl.th8.presidium.archive.TypeNotFoundException;
import nl.th8.presidium.archive.controller.dto.FilterDTO;
import nl.th8.presidium.archive.service.ArchiveService;
import nl.th8.presidium.home.controller.dto.KamerstukType;
import nl.th8.presidium.scheduler.KamerstukNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@SuppressWarnings({"SameReturnValue", "SpringMVCViewInspection"})
@Controller
@RequestMapping("/archive")
public class ArchiveController {

    private final ArchiveService archiveService;

    @Autowired
    public ArchiveController(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @GetMapping
    public String showArchive(Model model, Principal principal) {
        boolean loggedIn;
        try {
            loggedIn = principal.getName().length() > 0;
        } catch (NullPointerException e) {
            loggedIn = false;
        }

        model.addAttribute("types", KamerstukType.getPublics());
        model.addAttribute("loggedIn", loggedIn);
        return "select-archive";
    }

    @GetMapping("/{type}")
    public String showFilteredArchiveForType(Model model, @PathVariable String type, @RequestParam(name = "search", required = false) String search, Principal principal) {
        boolean loggedIn;
        try {
            loggedIn = principal.getName().length() > 0;
        } catch (NullPointerException e) {
            loggedIn = false;
        }

        try {
            if(StringUtils.isEmpty(search)) {
                model.addAttribute("kamerstukken", archiveService.getKamerstukkenForType(type));
            } else {
                model.addAttribute("kamerstukken", archiveService.getKamerstukkenForTypeFiltered(type, search));
            }
        } catch (TypeNotFoundException e) {
            return "redirect:/archive/" + type+ "?notfound";
        }
        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("current", type);
        model.addAttribute("searchTerm", search);

        return "archive";
    }

    @GetMapping(value = "/{type}/{id}", produces = "text/plain;charset=UTF-8")
    public String getMotion(Model model, @PathVariable String type, @PathVariable String id) {
        try {
            model.addAttribute("content", archiveService.getKamerstukHtml(type, id));
            model.addAttribute("callsign", archiveService.getNiceCallsign(type, id));
            return "kamerstuk";
        } catch (KamerstukNotFoundException e) {
            return "redirect:/archive/" + type+ "?notfound";
        }
    }
}
