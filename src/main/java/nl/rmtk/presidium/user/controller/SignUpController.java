package nl.rmtk.presidium.user.controller;

import nl.rmtk.presidium.user.InvalidSecretException;
import nl.rmtk.presidium.user.UsernameExistsException;
import nl.rmtk.presidium.user.controller.dto.User;
import nl.rmtk.presidium.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@SuppressWarnings({"SameReturnValue", "SpringMVCViewInspection"})
@Controller
@RequestMapping("/signup")
public class SignUpController {

    private final UserService userService;

    @Autowired
    public SignUpController(UserService userService) {
        this.userService = userService;
    }

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
