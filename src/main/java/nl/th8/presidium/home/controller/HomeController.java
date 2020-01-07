package nl.th8.presidium.home.controller;

import nl.th8.presidium.home.controller.dto.Kamerstuk;
import nl.th8.presidium.home.controller.dto.KamerstukType;
import nl.th8.presidium.home.controller.dto.StatDTO;
import nl.th8.presidium.home.service.StatsService;
import nl.th8.presidium.home.service.SubmitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.constraints.Null;
import java.security.Principal;

@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    SubmitService submitService;

    @Autowired
    StatsService statsService;

    @GetMapping
    public String showHome(Model model, Principal principal) {
        boolean loggedIn;
        try {
            loggedIn = principal.getName().length() > 0;
        } catch (NullPointerException e) {
            loggedIn = false;
        }

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("kamerstuk", new Kamerstuk());
        model.addAttribute("types", KamerstukType.getPublics());

        return "home";
    }

    @PostMapping
    public String submitKamerstuk(@ModelAttribute Kamerstuk kamerstuk) {
        try {
            submitService.processKamerstuk(kamerstuk);
        } catch (IllegalArgumentException e) {
            return "redirect:/?problem";
        }

        return "redirect:/?submitted";
    }

    @GetMapping("/stats")
    public String showStats(Model model) {
        StatDTO stats = statsService.getStats();
        model.addAttribute("stats", stats);

        return "stats";
    }
}
