package ru.boldycheva.budget.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.boldycheva.budget.dto.TransactionDto;
import ru.boldycheva.budget.entity.Transaction;
import ru.boldycheva.budget.service.AccountService;
import ru.boldycheva.budget.service.TransactionService;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @PostMapping("/transfer")
    public ResponseEntity<?> testTransfer(
            @RequestParam Long fromAccountId,
            @RequestParam Long toAccountId,
            @RequestParam BigDecimal amount) {
        try {
            TransactionDto dto = new TransactionDto();
            dto.setFromAccountId(fromAccountId);
            dto.setToAccountId(toAccountId);
            dto.setAmount(amount);
            dto.setTransactionType("TRANSFER");
            dto.setComment("Тестовый перевод");

            // Используем userId=1 и isAdmin=true для тестирования
            Transaction transaction = transactionService.createTransferTransaction(dto, 1L, true);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "transactionId", transaction.getId(),
                    "message", "Перевод успешно создан"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}