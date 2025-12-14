package ru.boldycheva.budget.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.boldycheva.budget.service.HomeService;

import java.security.Principal;

@Controller
public class HomeController {

    @Autowired
    private HomeService homeService;

    @GetMapping({"/", "/dashboard"})
    public String home(Model model, Principal principal) {
        return prepareDashboardModel(model, principal, "home");
    }

    private String prepareDashboardModel(Model model, Principal principal, String viewName) {
        // Получаем все данные через сервис
        HomeService.DashboardData dashboardData = homeService.prepareDashboardData(principal);

        // Передаем данные в модель
        model.addAttribute("username", dashboardData.getUsername());
        model.addAttribute("recentTransactions", dashboardData.getRecentTransactions());
        model.addAttribute("totalBalance", dashboardData.getTotalBalance());
        model.addAttribute("isAdmin", dashboardData.isAdmin());

        // Дополнительный атрибут, если нужен
        model.addAttribute("isGuest", dashboardData.isGuest());

        return viewName;
    }
}
