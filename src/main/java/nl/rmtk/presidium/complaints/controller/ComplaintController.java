package nl.th8.presidium.complaints.controller;

import nl.th8.presidium.ControllerUtils;
import nl.th8.presidium.complaints.InvalidComplaintException;
import nl.th8.presidium.complaints.controller.dto.Complaint;
import nl.th8.presidium.complaints.service.ComplaintService;
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
@RequestMapping("/klacht")
public class ComplaintController {

    private final ComplaintService complaintService;

    private final ControllerUtils controllerUtils;

    @Autowired
    public ComplaintController(ComplaintService complaintService, ControllerUtils controllerUtils) {
        this.complaintService = complaintService;
        this.controllerUtils = controllerUtils;
    }

    @GetMapping
    public String showComplaints(Model model, Principal principal) {
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

        boolean loggedIn = controllerUtils.checkLoggedIn(principal);

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("complaint", new Complaint());

        return "complaints";
    }

    @PostMapping
    public String submitComplaint(@ModelAttribute Complaint complaint) {
        Optional<String> redditStatus = controllerUtils.checkRedditStatus();
        if(redditStatus.isPresent())
            return redditStatus.get();

        try {
            complaintService.sendComplaint(complaint.getComplaintText(), complaint.getMessageLink());
        } catch (InvalidComplaintException e) {
            return "redirect:/klacht?invalid";
        }

        return "redirect:/klacht?done";
    }
}
