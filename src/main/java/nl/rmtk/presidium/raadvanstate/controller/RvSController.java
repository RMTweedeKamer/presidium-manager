package nl.rmtk.presidium.raadvanstate.controller;

import nl.rmtk.presidium.ControllerUtils;
import nl.rmtk.presidium.RedditSupplier;
import nl.rmtk.presidium.raadvanstate.controller.dto.AdviceDTO;
import nl.rmtk.presidium.scheduler.KamerstukNotFoundException;
import nl.rmtk.presidium.scheduler.service.KamerstukkenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;

@SuppressWarnings({"SameReturnValue", "SpringMVCViewInspection"})
@Controller
@RequestMapping("/rvs")
public class RvSController {


    private final KamerstukkenService kamerstukkenService;

    private final ControllerUtils controllerUtils;

    @Autowired
    public RvSController(KamerstukkenService kamerstukkenService, ControllerUtils controllerUtils) {
        this.kamerstukkenService = kamerstukkenService;
        this.controllerUtils = controllerUtils;
    }

    @GetMapping
    public String showRvS(Model model, Principal principal) {
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

        boolean loggedIn = controllerUtils.checkLoggedIn(principal);

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("queue", kamerstukkenService.getRvSQueue());
        model.addAttribute("adviceDTO", new AdviceDTO());


        return "rvs";
    }

    @PostMapping("/save")
    public String saveAdvice(@ModelAttribute AdviceDTO advice) {
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

        try {
            kamerstukkenService.saveAdvice(advice.getKamerstukId(), advice.getAdvice(), advice.sendNotification());
        } catch (KamerstukNotFoundException e) {
            return "redirect:/rvs?notfound";
        }

        return "redirect:/rvs?saved";
    }
}
