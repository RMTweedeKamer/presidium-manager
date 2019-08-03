package nl.th8.presidium.scheduler.controller;

import nl.th8.presidium.home.controller.dto.Kamerstuk;
import nl.th8.presidium.scheduler.service.KamerstukkenService;
import nl.th8.presidium.scheduler.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/scheduler")
public class SchedulerController {

    @Autowired
    KamerstukkenService kamerstukkenService;

    @Autowired
    NotificationService notificationService;

    @GetMapping
    public String showScheduler(Model model) {
        model.addAttribute("kamerstukken", kamerstukkenService.getNonScheduledKamerstukken());
        model.addAttribute("queue", kamerstukkenService.getKamerstukkenQueue());
        model.addAttribute("notifications", notificationService.getAllSettings().getNotifications());

        return "scheduler";
    }

    @PostMapping("/plan")
    public String planKamerstuk(@ModelAttribute Kamerstuk kamerstuk, @ModelAttribute String datetime) {
        kamerstukkenService.queueKamerstuk(kamerstuk);
        System.out.println("Put kamerstuk " + kamerstuk.getCallsign() + " in queue for " + kamerstuk.getPostDate());

        return "redirect:/scheduler?planned";
    }
}
