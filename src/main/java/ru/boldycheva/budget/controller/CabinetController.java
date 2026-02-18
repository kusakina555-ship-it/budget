package ru.boldycheva.budget.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CabinetController {

    @GetMapping("/cabinet")
    public String cabinet(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // Добавляем проверку
        if (userDetails == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", userDetails.getUsername());
        return "cabinet";
    }
}