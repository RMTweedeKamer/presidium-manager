package nl.rmtk.presidium.agenda.controller;

import nl.rmtk.presidium.scheduler.service.KamerstukkenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@SuppressWarnings("SameReturnValue")
@Controller
@RequestMapping("/agenda")
public class AgendaController {


    private final KamerstukkenService kamerstukkenService;

    @Autowired
    public AgendaController(KamerstukkenService kamerstukkenService) {
        this.kamerstukkenService = kamerstukkenService;
    }

    @GetMapping
    public String showAgenda(Model model, Principal principal) {
        boolean loggedIn;
        try {
            loggedIn = principal.getName().length() > 0;
        } catch (NullPointerException e) {
            loggedIn = false;
        }

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("queue", kamerstukkenService.getKamerstukkenQueue());
        model.addAttribute("voteQueue", kamerstukkenService.getKamerstukkenVoteQueue());

        return "agenda";
    }

    @GetMapping("/toukie")
    public String showForToukie(Model model, Principal principal) {
        boolean loggedIn;
        try {
            loggedIn = principal.getName().length() > 0;
        } catch (NullPointerException e) {
            loggedIn = false;
        }

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("queue", kamerstukkenService.getToukieQueue());

        return "toukie";
    }

    @GetMapping("/th8")
    public String showForTh8(Model model, Principal principal) {
        boolean loggedIn;
        try {
            loggedIn = principal.getName().length() > 0;
        } catch (NullPointerException e) {
            loggedIn = false;
        }

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("queue", kamerstukkenService.getTh8Queue());

        return "th8";
    }
}
