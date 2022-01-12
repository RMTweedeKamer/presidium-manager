package nl.th8.presidium.scheduler.controller;

import nl.th8.presidium.ControllerUtils;
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

        model.addAttribute("kamerstukken", kamerstukkenService.getNonScheduledKamerstukken(0, false));
        model.addAttribute("queue", kamerstukkenService.getKamerstukkenQueue());
        model.addAttribute("notifications", notificationService.getAllSettings().getNotifications());
        model.addAttribute("votesDelayed", kamerstukkenService.getDelayedKamerstukkenVoteQueue());
        model.addAttribute("votesQueued", kamerstukkenService.getKamerstukkenVoteQueue());

        return "scheduler";
    }

    @GetMapping("sidebar")
    public String getSidebar(Model model, @RequestParam(name = "page") String pageName) {
        model.addAttribute("pageName", pageName);
        model.addAttribute("inboxCount", kamerstukkenService.getNonScheduledCount());
        model.addAttribute("plannedCount", kamerstukkenService.getQueuedCount());
        model.addAttribute("voteCount", kamerstukkenService.getVoteCount());
        model.addAttribute("delayedCount", kamerstukkenService.getDelayedCount());
        model.addAttribute("deniedCount", kamerstukkenService.getDeniedCount());

        return "scheduler/sidebar";
    }

    @GetMapping("/inbox")
    public String showInbox(Model model, @RequestParam(defaultValue = "0") int filter, @RequestParam(required = false) boolean urgent) {
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

        model.addAttribute("items", kamerstukkenService.getNonScheduledKamerstukken(filter, urgent));

        return "scheduler/inbox";
    }

    @GetMapping("/planned")
    public String showPlannedList(Model model) {
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

        model.addAttribute("items", kamerstukkenService.getKamerstukkenQueue());

        return "scheduler/planned";
    }

    @GetMapping("/votequeue")
    public String showVoteList(Model model) {
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

        model.addAttribute("items", kamerstukkenService.getKamerstukkenVoteQueue());

        return "scheduler/votequeue";
    }

    @GetMapping("/delayed")
    public String showDelayedItems(Model model) {
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

        model.addAttribute("items", kamerstukkenService.getDelayedKamerstukkenVoteQueue());

        return "scheduler/delayed";
    }

    @GetMapping("/denied")
    public String showDeniedItems(Model model) {
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

        model.addAttribute("items", kamerstukkenService.getDeniedKamerstukken());

        return "scheduler/denied";
    }

    @PostMapping("/plan")
    public String planKamerstuk(@ModelAttribute Kamerstuk kamerstuk, Principal principal) {
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

        try {
            kamerstukkenService.queueKamerstuk(kamerstuk, principal.getName());
        } catch (InvalidUsernameException e) {
            return "redirect:/scheduler/inbox?invalidUsername";
        } catch (DuplicateCallsignException e) {
            return "redirect:/scheduler/inbox?duplicateCallsign";
        } catch (InvalidCallsignException e) {
            return "redirect:/scheduler/inbox?invalidCallsign";
        }
        logger.info("Put kamerstuk " + kamerstuk.getCallsign() + " in queue for " + kamerstuk.getPostDate());

        return "redirect:/scheduler/inbox?planned";
    }

    @PostMapping("/edit")
    public String editKamerstuk(@ModelAttribute Kamerstuk kamerstuk, Principal principal) {
        try {
            kamerstukkenService.editKamerstuk(kamerstuk.getId(), kamerstuk.getTitle(), kamerstuk.getBundleTitle(), kamerstuk.getContent(), kamerstuk.getToCallString(), principal.getName());
        } catch (KamerstukNotFoundException e) {
            return "redirect:/scheduler/planned?notfound";
        }

        return "redirect:/scheduler/planned?edited";
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
            return "redirect:/scheduler/planned?notfound";
        } catch (InvalidUsernameException e) {
            return "redirect:/scheduler/planned?invalidUsername";
        }
        return "redirect:/scheduler/planned?rescheduled";
    }

    @PostMapping("/rescheduleVote")
    public String rescheduleVote(@ModelAttribute Kamerstuk kamerstuk, Principal principal) {
        try {
            kamerstukkenService.rescheduleVote(kamerstuk.getId(), kamerstuk.getVoteDate(), principal.getName());
        }
        catch (KamerstukNotFoundException e) {
            return "redirect:/scheduler/votequeue?notfound";
        }
        return "redirect:/scheduler/votequeue?rescheduled";
    }

    @PostMapping("/unplan")
    public String unplanKamerstuk(@ModelAttribute Kamerstuk kamerstuk, Principal principal) {
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

        try {
            kamerstukkenService.dequeueKamerstuk(kamerstuk.getId(), kamerstuk.getReason(), principal.getName());
        } catch (KamerstukNotFoundException e) {
            return "redirect:/scheduler/planned?notfound";
        } catch (InvalidUsernameException e) {
            return "redirect:/scheduler/planned?invalidUsername";
        }

        logger.info("Removed kamerstuk " + kamerstuk.getCallsign() + " from queue");

        return "redirect:/scheduler/planned?unplanned";
    }

    @PostMapping("/deny")
    public String denyKamerstuk(@ModelAttribute Kamerstuk kamerstuk, Principal principal) {
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

        try {
            kamerstukkenService.denyKamerstuk(kamerstuk.getId(), kamerstuk.getReason(), principal.getName());
        } catch (KamerstukNotFoundException e) {
            return "redirect:/scheduler/inbox?notfound";
        } catch (InvalidUsernameException e) {
            return "redirect:/scheduler/inbox?invalidUsername";
        }
        logger.info("Denied kamerstuk " + kamerstuk.getTitle());

        return "redirect:/scheduler/inbox?denied";
    }

    @PostMapping("/withdraw")
    public String withdrawKamerstuk(@ModelAttribute Kamerstuk kamerstuk, Principal principal) {
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

        try {
            kamerstukkenService.withdrawKamerstuk(kamerstuk.getId(), principal.getName());
        } catch (KamerstukNotFoundException e) {
            return "redirect:/scheduler/planned?notfound";
        }
        logger.info("Withdrew kamerstuk " + kamerstuk.getTitle());

        return "redirect:/scheduler/planned?withdrawn";
    }

    @PostMapping("/setDelay")
    public String delayKamerstuk(@ModelAttribute Kamerstuk kamerstuk, Principal principal) {
        try {
            kamerstukkenService.delayKamerstuk(kamerstuk.getId(), principal.getName());
        } catch (KamerstukNotFoundException e) {
            return "redirect:/scheduler/votequeue?notfound";
        }
        logger.info("Delayed voting on kamerstuk " + kamerstuk.getTitle());

        return "redirect:/scheduler/votequeue?delayed";
    }
}
