package nl.rmtk.presidium.archive.controller;

import nl.rmtk.presidium.archive.TypeNotFoundException;
import nl.rmtk.presidium.archive.controller.dto.FilterDTO;
import nl.rmtk.presidium.archive.service.ArchiveService;
import nl.rmtk.presidium.home.controller.dto.KamerstukType;
import nl.rmtk.presidium.scheduler.KamerstukNotFoundException;
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
    String showArchive(Model model, Principal principal) {
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
    String showArchiveForType(Model model, @PathVariable String type, Principal principal) {
        boolean loggedIn;
        try {
            loggedIn = principal.getName().length() > 0;
        } catch (NullPointerException e) {
            loggedIn = false;
        }

        try {
            model.addAttribute("kamerstukken", archiveService.getKamerstukkenForType(type));
        } catch (TypeNotFoundException e) {
            return "redirect:/archive/" + type+ "?notfound";
        }
        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("current", type);
        model.addAttribute("filterDTO", new FilterDTO(""));

        return "archive";
    }

    @PostMapping("/{type}")
    String showFilteredArchiveForType(Model model, @PathVariable String type, @ModelAttribute FilterDTO filterDTO, Principal principal) {
        boolean loggedIn;
        try {
            loggedIn = principal.getName().length() > 0;
        } catch (NullPointerException e) {
            loggedIn = false;
        }

        System.out.println(filterDTO.getFilterString());

        try {
            model.addAttribute("kamerstukken", archiveService.getKamerstukkenForTypeFiltered(type, filterDTO.getFilterString()));
        } catch (TypeNotFoundException e) {
            return "redirect:/archive/" + type+ "?notfound";
        }
        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("current", type);
        model.addAttribute("filterDTO", filterDTO);

        return "archive";
    }

    @GetMapping(value = "/{type}/{id}", produces = "text/plain;charset=UTF-8")
    String getMotion(Model model, @PathVariable String type, @PathVariable String id) {
        try {
            model.addAttribute("content", archiveService.getKamerstukHtml(type, id));
            model.addAttribute("callsign", archiveService.getNiceCallsign(type, id));
            return "kamerstuk";
        } catch (KamerstukNotFoundException e) {
            return "redirect:/archive/" + type+ "?notfound";
        }
    }
}
