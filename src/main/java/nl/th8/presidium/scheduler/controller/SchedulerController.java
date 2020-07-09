package nl.th8.presidium.scheduler.controller;

import nl.th8.presidium.Constants;
import nl.th8.presidium.home.controller.dto.Kamerstuk;
import nl.th8.presidium.scheduler.DuplicateCallsignException;
import nl.th8.presidium.scheduler.InvalidCallsignException;
import nl.th8.presidium.scheduler.InvalidUsernameException;
import nl.th8.presidium.scheduler.KamerstukNotFoundException;
import nl.th8.presidium.scheduler.service.KamerstukkenService;
import nl.th8.presidium.scheduler.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@Controller
@RequestMapping("/scheduler")
public class SchedulerController {

    private Logger logger = LoggerFactory.getLogger(SchedulerController.class);

    @Autowired
    KamerstukkenService kamerstukkenService;

    @Autowired
    NotificationService notificationService;

    @GetMapping
    public String showScheduler(Model model) {
        model.addAttribute("kamerstukken", kamerstukkenService.getNonScheduledKamerstukken());
        model.addAttribute("queue", kamerstukkenService.getKamerstukkenQueue());
        model.addAttribute("notifications", notificationService.getAllSettings().getNotifications());
        model.addAttribute("votesDelayed", kamerstukkenService.getDelayedKamerstukkenVoteQueue());
        model.addAttribute("votesQueued", kamerstukkenService.getKamerstukkenVoteQueue());

        return "scheduler";
    }

    @PostMapping("/plan")
    public String planKamerstuk(@ModelAttribute Kamerstuk kamerstuk, Principal principal) {
        try {
            kamerstukkenService.queueKamerstuk(kamerstuk, principal.getName());
        } catch (InvalidUsernameException e) {
            return "redirect:/scheduler?invalidUsername";
        } catch (DuplicateCallsignException e) {
            return "redirect:/scheduler?duplicateCallsign";
        } catch (InvalidCallsignException e) {
            return "redirect:/scheduler?invalidCallsign";
        }
        logger.info("Put kamerstuk " + kamerstuk.getCallsign() + " in queue for " + kamerstuk.getPostDate());

        return "redirect:/scheduler?planned";
    }

    @PostMapping("/edit")
    public String editKamerstuk(@ModelAttribute Kamerstuk kamerstuk, Principal principal) {
        try {
            kamerstukkenService.editKamerstuk(kamerstuk.getId(), kamerstuk.getTitle(), kamerstuk.getContent(), kamerstuk.getToCallString(), principal.getName());
        } catch (KamerstukNotFoundException e) {
            return "redirect:/scheduler?notfound";
        }
        logger.info("Edited kamerstuk: " + kamerstuk.getCallsign() + " scheduled for " + kamerstuk.getPostDate());

        return "redirect:/scheduler?edited";
    }

//    @PostMapping("/plan/api")
//    public ResponseEntity planKamerstukAPI(@RequestBody @Valid Kamerstuk kamerstuk) {
//        String id;
//        if(kamerstuk.getSecret().equals()) {
//            kamerstuk.processToCallString();
//            try {
//                id = kamerstukkenService.queueKamerstuk(kamerstuk, "API");
//            } catch (InvalidUsernameException | DuplicateCallsignException e) {
//                return ResponseEntity.status(400).build();
//            }
//            logger.info("Put kamerstuk " + kamerstuk.getCallsign() + " in queue for " + kamerstuk.getPostDate());
//        }
//        else {
//            return ResponseEntity.status(401).build();
//        }
//        return ResponseEntity.status(201).body(id);
//    }

    @PostMapping("/reschedule")
    public String rescheduleKamerstuk(@ModelAttribute Kamerstuk kamerstuk, Principal principal) {
        try {
            kamerstukkenService.rescheduleKamerstuk(kamerstuk.getId(), kamerstuk.getPostDate(), kamerstuk.getVoteDate(), principal.getName());
        }
        catch (KamerstukNotFoundException e) {
            return "redirect:/scheduler?notfound";
        } catch (InvalidUsernameException e) {
            return "redirect:/scheduler?invalidUsername";
        }
        return "redirect:/scheduler?rescheduled";
    }

    @PostMapping("/rescheduleVote")
    public String rescheduleVote(@ModelAttribute Kamerstuk kamerstuk, Principal principal) {
        try {
            kamerstukkenService.rescheduleVote(kamerstuk.getId(), kamerstuk.getVoteDate(), principal.getName());
        }
        catch (KamerstukNotFoundException e) {
            return "redirect:/scheduler?notfound";
        }
        return "redirect:/scheduler?rescheduled";
    }

    @PostMapping("/unplan")
    public String unplanKamerstuk(@ModelAttribute Kamerstuk kamerstuk, Principal principal) {
        try {
            kamerstukkenService.dequeueKamerstuk(kamerstuk.getId(), kamerstuk.getReason(), principal.getName());
        } catch (KamerstukNotFoundException e) {
            return "redirect:/scheduler?notfound";
        } catch (InvalidUsernameException e) {
            return "redirect:/scheduler?invalidUsername";
        }

        logger.info("Removed kamerstuk " + kamerstuk.getCallsign() + " from queue");

        return "redirect:/scheduler?unplanned";
    }

    @PostMapping("/deny")
    public String denyKamerstuk(@ModelAttribute Kamerstuk kamerstuk, Principal principal) {
        try {
            kamerstukkenService.denyKamerstuk(kamerstuk.getId(), kamerstuk.getReason(), principal.getName());
        } catch (KamerstukNotFoundException e) {
            return "redirect:/scheduler?notfound";
        } catch (InvalidUsernameException e) {
            return "redirect:/scheduler?invalidUsername";
        }
        logger.info("Denied kamerstuk " + kamerstuk.getTitle());

        return "redirect:/scheduler?denied";
    }

    @PostMapping("/withdraw")
    public String withdrawKamerstuk(@ModelAttribute Kamerstuk kamerstuk, Principal principal) {
        try {
            kamerstukkenService.withdrawKamerstuk(kamerstuk.getId(), principal.getName());
        } catch (KamerstukNotFoundException e) {
            return "redirect:/scheduler?notfound";
        }
        logger.info("Withdrew kamerstuk " + kamerstuk.getTitle());

        return "redirect:/scheduler?withdrawn";
    }

    @PostMapping("/setDelay")
    public String delayKamerstuk(@ModelAttribute Kamerstuk kamerstuk, Principal principal) {
        try {
            kamerstukkenService.delayKamerstuk(kamerstuk.getId(), principal.getName());
        } catch (KamerstukNotFoundException e) {
            return "redirect:/scheduler?notfound";
        }
        logger.info("Delayed voting on kamerstuk " + kamerstuk.getTitle());

        return "redirect:/scheduler?delayed";
    }
}
