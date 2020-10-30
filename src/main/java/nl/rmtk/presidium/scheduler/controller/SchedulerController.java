package nl.rmtk.presidium.scheduler.controller;

import nl.rmtk.presidium.ControllerUtils;
import nl.rmtk.presidium.home.controller.dto.Kamerstuk;
import nl.rmtk.presidium.scheduler.DuplicateCallsignException;
import nl.rmtk.presidium.scheduler.InvalidCallsignException;
import nl.rmtk.presidium.scheduler.InvalidUsernameException;
import nl.rmtk.presidium.scheduler.KamerstukNotFoundException;
import nl.rmtk.presidium.scheduler.service.KamerstukkenService;
import nl.rmtk.presidium.scheduler.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@SuppressWarnings({"SameReturnValue", "SpringMVCViewInspection"})
@Controller
@RequestMapping("/scheduler")
public class SchedulerController {

    private final Logger logger = LoggerFactory.getLogger(SchedulerController.class);

    private final KamerstukkenService kamerstukkenService;

    private final NotificationService notificationService;

    private final ControllerUtils controllerUtils;

    @Autowired
    public SchedulerController(KamerstukkenService kamerstukkenService, NotificationService notificationService, ControllerUtils controllerUtils) {
        this.kamerstukkenService = kamerstukkenService;
        this.notificationService = notificationService;
        this.controllerUtils = controllerUtils;
    }

    @GetMapping
    public String showScheduler(Model model) {
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

        model.addAttribute("kamerstukken", kamerstukkenService.getNonScheduledKamerstukken());
        model.addAttribute("queue", kamerstukkenService.getKamerstukkenQueue());
        model.addAttribute("notifications", notificationService.getAllSettings().getNotifications());
        model.addAttribute("votesDelayed", kamerstukkenService.getDelayedKamerstukkenVoteQueue());
        model.addAttribute("votesQueued", kamerstukkenService.getKamerstukkenVoteQueue());

        return "scheduler";
    }

    @PostMapping("/plan")
    public String planKamerstuk(@ModelAttribute Kamerstuk kamerstuk, Principal principal) {
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

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
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

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
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

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
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

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
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

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
