package ru.boldycheva.budget.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.boldycheva.budget.security.UserPrincipal;

@Controller
public class CabinetController {

    @GetMapping("/cabinet")
    public String cabinet(@AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        model.addAttribute("username", userPrincipal.getUsername());
        return "cabinet";
    }
}