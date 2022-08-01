package nl.th8.presidium.scheduler.controller;

import nl.th8.presidium.ControllerUtils;
import nl.th8.presidium.home.controller.dto.Kamerstuk;
import nl.th8.presidium.scheduler.service.KamerstukkenService;
import nl.th8.presidium.scheduler.service.NotificationService;
import nl.th8.presidium.scheduler.service.SettingsProvider;
import nl.th8.presidium.scheduler.service.VoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/scheduler")
public class VoteController {

    private final Logger logger = LoggerFactory.getLogger(VoteController.class);

    private final SettingsProvider settingsProvider;

    private final VoteService voteService;

    @Autowired
    public VoteController(SettingsProvider settingsProvider, VoteService voteService) {
        this.settingsProvider = settingsProvider;
        this.voteService = voteService;
    }

    @GetMapping("/voteSettings")
    public String showSettings(Model model) {
        model.addAttribute("tkSetting", settingsProvider.getTkMembers());
        model.addAttribute("results", voteService.getToPostResults());

        return "scheduler/voteSettings";
    }

    @PostMapping("/voteSettings")
    public String saveSettings(Model model, String newTkMemberString) {

        settingsProvider.setTkMembers(newTkMemberString);

        return "redirect:/scheduler/voteSettings";
    }

    @PostMapping("/voteSettings/doNow")
    public String processVoteNow(Model model, String url) {
        try {
            voteService.processVoteResultsForUrl(url);
        } catch (NoSuchElementException e) {
            return "redirect:/scheduler/voteSettings?notfound";
        }

        return "redirect:/scheduler/voteSettings?done";
    }
}
