package nl.th8.presidium.home.controller;

import nl.th8.presidium.Constants;
import nl.th8.presidium.home.controller.dto.Kamerstuk;
import nl.th8.presidium.home.controller.dto.KamerstukType;
import nl.th8.presidium.home.controller.dto.StatDTO;
import nl.th8.presidium.home.service.StatsService;
import nl.th8.presidium.home.service.SubmitService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.security.Principal;
import java.util.Objects;

@SuppressWarnings({"SameReturnValue", "SpringMVCViewInspection"})
@Controller
@RequestMapping("/")
public class HomeController {

    private final SubmitService submitService;

    private final StatsService statsService;

    @Autowired
    public HomeController(SubmitService submitService, StatsService statsService) {
        this.submitService = submitService;
        this.statsService = statsService;
    }

    @GetMapping
    public String showHome(Model model, Principal principal) {
        boolean loggedIn;
        try {
            loggedIn = principal.getName().length() > 0;
        } catch (NullPointerException e) {
            loggedIn = false;
        }

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("types", KamerstukType.getPublics());
        if(!model.containsAttribute("kamerstuk"))
            model.addAttribute("kamerstuk", new Kamerstuk());

        return "home";
    }

    @PostMapping
    public String submitKamerstuk(@ModelAttribute Kamerstuk kamerstuk, @RequestParam(value = "spamcheck") String spamcheck, RedirectAttributes redirectAttributes) {
        try {
            if(!StringUtils.equalsIgnoreCase(spamcheck, Constants.SPAM_ANSWER)) {
                redirectAttributes.addFlashAttribute(kamerstuk);
                return "redirect:/?spam";
            }
            submitService.processKamerstuk(kamerstuk);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(kamerstuk);
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
