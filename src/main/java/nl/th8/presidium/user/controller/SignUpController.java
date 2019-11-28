package nl.th8.presidium.user.controller;

import nl.th8.presidium.user.InvalidSecretException;
import nl.th8.presidium.user.UsernameExistsException;
import nl.th8.presidium.user.controller.dto.User;
import nl.th8.presidium.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequestMapping("/signup")
public class SignUpController {

    @Autowired
    UserService userService;

    @GetMapping
    public String showSignUp(@ModelAttribute("newUser")User user, Model model) {
        return "signup";
    }

    @PostMapping
    public String signUp(@ModelAttribute @Valid User user) {
        try {
            userService.createUser(user);
        } catch (UsernameExistsException e) {
            return "redirect:/signup?nameerror";
        } catch (InvalidSecretException e) {
            return "redirect:/signup?invalidsecret";
        }
        return "login";
    }

}
