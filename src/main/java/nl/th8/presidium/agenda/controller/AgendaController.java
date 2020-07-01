package nl.th8.presidium.agenda.controller;

import nl.th8.presidium.scheduler.service.KamerstukkenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/agenda")
public class AgendaController {


    @Autowired
    KamerstukkenService kamerstukkenService;

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
}
