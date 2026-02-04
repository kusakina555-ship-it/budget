package ru.boldycheva.budget.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldycheva.budget.dto.TransactionDto;
import ru.boldycheva.budget.dto.TransactionDisplayDto;
import ru.boldycheva.budget.entity.*;
import ru.boldycheva.budget.repository.AccountRepository;
import ru.boldycheva.budget.repository.CategoryRepository;
import ru.boldycheva.budget.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccountService accountService;

    // Создание перевода (две транзакции)
    @Transactional
    public Transaction createTransferTransaction(TransactionDto transactionDto, Long userId, boolean isAdmin) {
        logger.info("Создание перевода: fromAccountId={}, toAccountId={}, amount={}, userId={}, isAdmin={}",
                transactionDto.getFromAccountId(), transactionDto.getToAccountId(),
                transactionDto.getAmount(), userId, isAdmin);

        // Валидация
        if (transactionDto.getFromAccountId() == null || transactionDto.getToAccountId() == null) {
            throw new IllegalArgumentException("Не указаны счета для перевода");
        }

        if (transactionDto.getFromAccountId().equals(transactionDto.getToAccountId())) {
            throw new IllegalArgumentException("Нельзя перевести средства на тот же счет");
        }

        if (transactionDto.getAmount() == null || transactionDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");
        }

        // Получаем счета
        Account fromAccount = accountRepository.findById(transactionDto.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("Исходный счет не найден"));
        Account toAccount = accountRepository.findById(transactionDto.getToAccountId())
                .orElseThrow(() -> new RuntimeException("Целевой счет не найден"));

        // Проверяем права доступа
        if (!isAdmin && !fromAccount.getUser().getId().equals(userId)) {
            throw new RuntimeException("Исходный счет не принадлежит текущему пользователю");
        }

        // Проверяем разные валюты
        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new RuntimeException("Переводы между счетами в разных валютах пока не поддерживаются");
        }

        // Проверяем достаточность средств
        if (fromAccount.getBalance().compareTo(transactionDto.getAmount()) < 0) {
            throw new RuntimeException("Недостаточно средств на исходном счете. Доступно: " +
                    fromAccount.getBalance() + " " + fromAccount.getCurrency());
        }

        // Получаем категорию "Переводы"
        Category transferCategory = categoryRepository.findByName("Переводы")
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName("Переводы");
                    newCategory.setCategoryType("TRANSFER");
                    return categoryRepository.save(newCategory);
                });

        LocalDateTime now = LocalDateTime.now();

        // 1. Создаем транзакцию для отправителя (минус)
        Transaction senderTransaction = new Transaction();
        senderTransaction.setAmount(transactionDto.getAmount().negate()); // Отрицательная сумма
        senderTransaction.setComment(transactionDto.getComment() != null ?
                transactionDto.getComment() : "Перевод на счет #" + toAccount.getId());
        senderTransaction.setTransactionDate(now);
        senderTransaction.setTransactionType(TransactionType.TRANSFER);
        senderTransaction.setCategory(transferCategory);
        senderTransaction.setAccount(fromAccount);
        senderTransaction.setCounterpartyAccount(toAccount); // Кому перевели

        // 2. Создаем транзакцию для получателя (плюс)
        Transaction receiverTransaction = new Transaction();
        receiverTransaction.setAmount(transactionDto.getAmount()); // Положительная сумма
        receiverTransaction.setComment(transactionDto.getComment() != null ?
                transactionDto.getComment() : "Перевод со счета #" + fromAccount.getId());
        receiverTransaction.setTransactionDate(now);
        receiverTransaction.setTransactionType(TransactionType.TRANSFER);
        receiverTransaction.setCategory(transferCategory);
        receiverTransaction.setAccount(toAccount);
        receiverTransaction.setCounterpartyAccount(fromAccount); // От кого перевели

        // Сохраняем сначала транзакцию отправителя
        Transaction savedSenderTransaction = transactionRepository.save(senderTransaction);

        // Затем транзакцию получателя с ссылкой на связанную транзакцию
        receiverTransaction.setRelatedTransactionId(savedSenderTransaction.getId());
        Transaction savedReceiverTransaction = transactionRepository.save(receiverTransaction);

        // Обновляем отправителя ссылкой на связанную транзакцию
        savedSenderTransaction.setRelatedTransactionId(savedReceiverTransaction.getId());
        transactionRepository.save(savedSenderTransaction);

        // Обновляем балансы счетов
        fromAccount.setBalance(fromAccount.getBalance().subtract(transactionDto.getAmount()));
        accountRepository.save(fromAccount);

        toAccount.setBalance(toAccount.getBalance().add(transactionDto.getAmount()));
        accountRepository.save(toAccount);

        logger.info("Перевод создан: senderTransactionId={}, receiverTransactionId={}",
                savedSenderTransaction.getId(), savedReceiverTransaction.getId());

        return savedSenderTransaction;
    }

    // Получение всех транзакций для отображения
    public List<TransactionDisplayDto> getAllTransactionsForDisplay() {
        List<Transaction> transactions = transactionRepository.findAllByOrderByTransactionDateDesc();
        return convertToDisplayDtoList(transactions);
    }

    @Transactional
    public Transaction createIncomeExpenseTransaction(TransactionDto transactionDto, Long userId, boolean isAdmin) {
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

        // Проверяем права доступа (только если не админ)
        if (!isAdmin && !account.getUser().getId().equals(userId)) {
            throw new RuntimeException("Счет не принадлежит текущему пользователю");
        }

        // Создаем транзакцию
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDto.getAmount());
        transaction.setComment(transactionDto.getComment());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionType(TransactionType.valueOf(transactionDto.getTransactionType()));
        transaction.setCategory(category);
        transaction.setAccount(account); // Используем setAccount вместо setAccountId

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Обновляем баланс счета
        updateAccountBalance(account, savedTransaction);

        return savedTransaction;
    }

    @Transactional
    public void deleteTransaction(Long transactionId, Long userId, boolean isAdmin) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Транзакция не найдена"));

        // Проверяем, является ли это переводом
        if (transaction.getTransactionType() == TransactionType.TRANSFER &&
                transaction.getRelatedTransactionId() != null) {

            // Для переводов удаляем обе транзакции
            Transaction relatedTransaction = transactionRepository.findById(transaction.getRelatedTransactionId())
                    .orElse(null);

            if (relatedTransaction != null) {
                // Проверяем права доступа для обеих транзакций
                if (!isAdmin) {
                    if (!transaction.getAccount().getUser().getId().equals(userId) &&
                            !relatedTransaction.getAccount().getUser().getId().equals(userId)) {
                        throw new RuntimeException("Транзакция не принадлежит текущему пользователю");
                    }
                }

                // Возвращаем средства на счета
                Account account1 = transaction.getAccount();
                Account account2 = relatedTransaction.getAccount();

                // Возвращаем средства (инвертируем операции)
                account1.setBalance(account1.getBalance().add(transaction.getAmount().abs()));
                accountRepository.save(account1);

                account2.setBalance(account2.getBalance().subtract(relatedTransaction.getAmount().abs()));
                accountRepository.save(account2);

                // Удаляем обе транзакции
                transactionRepository.delete(relatedTransaction);
            }
        } else {
            // Для доходов/расходов
            Account account = transaction.getAccount();

            if (account != null) {
                // Проверяем права доступа (только если не админ)
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
                }
            }
        }
        transactionRepository.delete(transaction);
    }

    public List<Transaction> getRecentTransactions(int limit) {
        return transactionRepository.findTopNByOrderByTransactionDateDesc(limit);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByTransactionDateDesc();
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

    public List<TransactionDisplayDto> getRecentTransactionsForDisplay(int limit) {
        List<Transaction> transactions = transactionRepository.findTopNByOrderByTransactionDateDesc(limit);
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

        // Категория
        if (transaction.getCategory() != null) {
            dto.setCategoryName(getCategoryFullPath(transaction.getCategory()));
        } else {
            dto.setCategoryName("Без категории");
        }

        // Счет
        if (transaction.getAccount() != null) {
            dto.setAccountName(transaction.getAccount().getDisplayName());
            dto.setCurrency(transaction.getAccount().getCurrency());

            // Для переводов добавляем информацию о контрагенте
            if (transaction.getTransactionType() == TransactionType.TRANSFER &&
                    transaction.getCounterpartyAccount() != null) {
                dto.setCounterpartyAccountName(transaction.getCounterpartyAccount().getDisplayName());
            }
        } else {
            dto.setCurrency("RUB"); // Значение по умолчанию
        }

        // Связанная транзакция
        dto.setRelatedTransactionId(transaction.getRelatedTransactionId());

        return dto;
    }

    private String getCategoryFullPath(Category category) {
        if (category.getParent() != null) {
            // Если это подкатегория, показываем родителя → подкатегорию
            return category.getParent().getName() + " → " + category.getName();
        }
        return category.getName();
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Транзакция не найдена"));
    }
}