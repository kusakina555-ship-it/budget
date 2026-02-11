package ru.boldycheva.mainapp.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.boldycheva.mainapp.dto.ApiResponse;
import ru.boldycheva.mainapp.dto.CategoryDto;
import ru.boldycheva.mainapp.dto.TransactionDto;
import ru.boldycheva.mainapp.service.TransactionControllerService;
import ru.boldycheva.mainapp.service.TransactionManagementService;
import ru.boldycheva.mainapp.service.TransactionService;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionManagementService transactionManagementService;

    @Autowired
    private TransactionControllerService transactionControllerService;

    // === HTML СТРАНИЦЫ ===

    @GetMapping("/new")
    public String showTransactionForm(
            @RequestParam(value = "type", required = false) String transactionType,
            Model model,
            Authentication authentication) {

        return transactionControllerService.prepareTransactionForm(model, authentication, transactionType);
    }

    @PostMapping("/new")
    public String createTransaction(
            @Valid @ModelAttribute TransactionDto transactionDto,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        return transactionControllerService.processTransactionCreation(
                transactionDto, bindingResult, authentication, model, redirectAttributes);
    }

    @GetMapping
    public String getAllTransactions(Model model) {
        model.addAttribute("transactions", transactionService.getAllTransactionsForDisplay());
        return "transactions/list";
    }

    @GetMapping("/{id}")
    public String getTransactionDetails(@PathVariable Long id, Model model) {
        model.addAttribute("transaction", transactionService.getTransactionById(id));
        return "transactions/details";
    }

    @PostMapping("/{id}/delete")
    public String deleteTransaction(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        return transactionControllerService.processTransactionDeletion(id, authentication, redirectAttributes);
    }

    // === API ENDPOINTS (для AJAX) ===

    @GetMapping("/api/categories/type/{type}")
    @ResponseBody
    public ResponseEntity<ApiResponse<?>> getCategoriesByType(
            @PathVariable String type,
            Authentication authentication) {

        return transactionControllerService.getCategoriesByType(type, authentication);
    }

    @GetMapping("/api/categories/{parentId}/subcategories")
    @ResponseBody
    public ResponseEntity<ApiResponse<?>> getSubcategories(
            @PathVariable Long parentId,
            Authentication authentication) {

        return transactionControllerService.getSubcategories(parentId, authentication);
    }

    @PostMapping("/api/categories")
    @ResponseBody
    public ResponseEntity<ApiResponse<?>> createCategory(
            @RequestBody CategoryDto categoryDto,
            Authentication authentication) {

        return transactionControllerService.createCategory(categoryDto, authentication);
    }
}