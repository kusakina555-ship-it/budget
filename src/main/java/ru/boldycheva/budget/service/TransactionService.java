package ru.boldycheva.budget.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldycheva.budget.dto.TransactionDisplayDto;
import ru.boldycheva.budget.dto.TransactionDto;
import ru.boldycheva.budget.entity.*;
import ru.boldycheva.budget.repository.AccountRepository;
import ru.boldycheva.budget.repository.CategoryRepository;
import ru.boldycheva.budget.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccountRepository accountRepository;

    public List<Transaction> getRecentTransactions(int limit) {
        return transactionRepository.findTopNByOrderByTransactionDateDesc(limit);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByTransactionDateDesc();
    }

    @Transactional
    public Transaction createIncomeExpenseTransaction(TransactionDto transactionDto, Long userId) {
        // Валидация
        if (transactionDto.getAccountId() == null) {
            throw new IllegalArgumentException("Не указан счет для операции");
        }

        // Получаем категорию
        Category category = categoryRepository.findById(transactionDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        // Получаем счет
        Account account = accountRepository.findById(transactionDto.getAccountId())
                .orElseThrow(() -> new RuntimeException("Счет не найден"));

        // Проверяем, что счет принадлежит пользователю
        if (!account.getUser().getId().equals(userId)) {
            throw new RuntimeException("Счет не принадлежит текущему пользователю");
        }

        // Создаем транзакцию
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDto.getAmount());
        transaction.setComment(transactionDto.getComment());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionType(TransactionType.valueOf(transactionDto.getTransactionType()));
        transaction.setCategory(category);
        transaction.setAccountId(account.getId()); // Сохраняем только ID счета

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Обновляем баланс счета
        updateAccountBalance(account, savedTransaction);

        return savedTransaction;
    }

    @Transactional
    public Transaction createTransferTransaction(TransactionDto transactionDto, Long userId) {
        // Валидация для переводов
        if (transactionDto.getFromAccountId() == null || transactionDto.getToAccountId() == null) {
            throw new IllegalArgumentException("Не указаны счета для перевода");
        }

        if (transactionDto.getFromAccountId().equals(transactionDto.getToAccountId())) {
            throw new IllegalArgumentException("Нельзя перевести средства на тот же счет");
        }

        // Получаем счета
        Account fromAccount = accountRepository.findById(transactionDto.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("Исходный счет не найден"));
        Account toAccount = accountRepository.findById(transactionDto.getToAccountId())
                .orElseThrow(() -> new RuntimeException("Целевой счет не найден"));

        // Проверяем, что счета принадлежат пользователю
        if (!fromAccount.getUser().getId().equals(userId) ||
                !toAccount.getUser().getId().equals(userId)) {
            throw new RuntimeException("Счета не принадлежат текущему пользователю");
        }

        // Проверяем достаточность средств
        if (fromAccount.getBalance().compareTo(transactionDto.getAmount()) < 0) {
            throw new RuntimeException("Недостаточно средств на исходном счете");
        }

        // Получаем категорию "Переводы"
        Category transferCategory = categoryRepository.findByName("Переводы")
                .orElseThrow(() -> new RuntimeException("Категория 'Переводы' не найдена"));

        // Создаем транзакцию перевода
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDto.getAmount());
        transaction.setComment(transactionDto.getComment() != null ?
                transactionDto.getComment() : "Перевод между счетами");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setCategory(transferCategory);
        transaction.setFromAccountId(fromAccount.getId()); // Сохраняем ID счетов
        transaction.setToAccountId(toAccount.getId());

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Обновляем балансы счетов
        fromAccount.setBalance(fromAccount.getBalance().subtract(transactionDto.getAmount()));
        accountRepository.save(fromAccount);

        toAccount.setBalance(toAccount.getBalance().add(transactionDto.getAmount()));
        accountRepository.save(toAccount);

        return savedTransaction;
    }

    private void updateAccountBalance(Account account, Transaction transaction) {
        BigDecimal newBalance;

        if (transaction.getTransactionType() == TransactionType.INCOME) {
            newBalance = account.getBalance().add(transaction.getAmount());
        } else if (transaction.getTransactionType() == TransactionType.EXPENSE) {
            if (account.getBalance().compareTo(transaction.getAmount()) < 0) {
                throw new RuntimeException("Недостаточно средств на счете");
            }
            newBalance = account.getBalance().subtract(transaction.getAmount());
        } else {
            throw new IllegalArgumentException("Неподдерживаемый тип транзакции");
        }

        account.setBalance(newBalance);
        accountRepository.save(account);
    }

    @Transactional
    public void deleteTransaction(Long transactionId, Long userId, boolean isAdmin) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Транзакция не найдена"));

        // Для простоты - проверяем права доступа по accountId
        // В реальном приложении нужна более сложная логика

        // Получаем счет для проверки прав
        if (transaction.getAccountId() != null) {
            Account account = accountRepository.findById(transaction.getAccountId())
                    .orElse(null);

            if (account != null) {
                // Проверяем права доступа
                if (!isAdmin && !account.getUser().getId().equals(userId)) {
                    throw new RuntimeException("Транзакция не принадлежит текущему пользователю");
                }

                // Возвращаем баланс
                if (transaction.getTransactionType() == TransactionType.INCOME) {
                    account.setBalance(account.getBalance().subtract(transaction.getAmount()));
                    accountRepository.save(account);
                } else if (transaction.getTransactionType() == TransactionType.EXPENSE) {
                    account.setBalance(account.getBalance().add(transaction.getAmount()));
                    accountRepository.save(account);
                } else if (transaction.getTransactionType() == TransactionType.TRANSFER) {
                    // Для переводов нужна более сложная логика
                    if (transaction.getFromAccountId() != null) {
                        Account fromAccount = accountRepository.findById(transaction.getFromAccountId())
                                .orElse(null);
                        if (fromAccount != null) {
                            fromAccount.setBalance(fromAccount.getBalance().add(transaction.getAmount()));
                            accountRepository.save(fromAccount);
                        }
                    }
                    if (transaction.getToAccountId() != null) {
                        Account toAccount = accountRepository.findById(transaction.getToAccountId())
                                .orElse(null);
                        if (toAccount != null) {
                            toAccount.setBalance(toAccount.getBalance().subtract(transaction.getAmount()));
                            accountRepository.save(toAccount);
                        }
                    }
                }
            }
        }

        transactionRepository.delete(transaction);
    }

    public List<TransactionDisplayDto> getRecentTransactionsForDisplay(int limit) {
        List<Transaction> transactions = transactionRepository.findTopNByOrderByTransactionDateDesc(limit);
        return convertToDisplayDtoList(transactions);
    }

    public List<TransactionDisplayDto> getAllTransactionsForDisplay() {
        List<Transaction> transactions = transactionRepository.findAllByOrderByTransactionDateDesc();
        return convertToDisplayDtoList(transactions);
    }

    private List<TransactionDisplayDto> convertToDisplayDtoList(List<Transaction> transactions) {
        return transactions.stream()
                .map(this::convertToDisplayDto)
                .collect(Collectors.toList());
    }

    private TransactionDisplayDto convertToDisplayDto(Transaction transaction) {
        TransactionDisplayDto dto = new TransactionDisplayDto();
        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setComment(transaction.getComment());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setTransactionType(transaction.getTransactionType());

        // Получаем имя категории
        if (transaction.getCategory() != null) {
            dto.setCategoryName(transaction.getCategory().getName());
        } else {
            dto.setCategoryName("Без категории");
        }

        // Получаем валюту через AccountService
        String currency = "RUB";
        if (transaction.getAccountId() != null) {
            currency = accountService.getCurrencyByAccountId(transaction.getAccountId());
        } else if (transaction.getFromAccountId() != null) {
            currency = accountService.getCurrencyByAccountId(transaction.getFromAccountId());
        }
        dto.setCurrency(currency);

        return dto;
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Транзакция не найдена"));
    }
}