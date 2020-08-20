package nl.th8.presidium.raadvanstate.controller;

import nl.th8.presidium.home.controller.dto.Kamerstuk;
import nl.th8.presidium.scheduler.DuplicateCallsignException;
import nl.th8.presidium.scheduler.InvalidCallsignException;
import nl.th8.presidium.scheduler.InvalidUsernameException;
import nl.th8.presidium.scheduler.KamerstukNotFoundException;
import nl.th8.presidium.scheduler.service.KamerstukkenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/rvs")
public class RvSController {


    @Autowired
    KamerstukkenService kamerstukkenService;

    @GetMapping
    public String showRvS(Model model, Principal principal) {
        boolean loggedIn;
        try {
            loggedIn = principal.getName().length() > 0;
        } catch (NullPointerException e) {
            loggedIn = false;
        }

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("queue", kamerstukkenService.getRvSQueue());

        return "rvs";
    }

    @PostMapping("/save")
    public String saveAdvice(@ModelAttribute Kamerstuk kamerstuk, String secret, Principal principal) {
        try {
            kamerstukkenService.saveAdvice(kamerstuk.getId(), kamerstuk.getAdvice());
        } catch (KamerstukNotFoundException e) {
            return "redirect:/rvs?notfound";
        }

        return "redirect:/rvs?saved";
    }
}
