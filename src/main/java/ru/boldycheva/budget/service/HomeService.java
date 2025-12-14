package ru.boldycheva.budget.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldycheva.budget.entity.Transaction;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Service
public class HomeService {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    /**
     * Подготавливает данные для отображения на дашборде
     *
     * @param principal текущий аутентифицированный пользователь
     * @return DashboardData объект с подготовленными данными
     */
    public DashboardData prepareDashboardData(Principal principal) {
        DashboardData dashboardData = new DashboardData();

        if (principal != null) {
            String username = principal.getName();
            dashboardData.setUsername(username);
            dashboardData.setGuest(false);

            // Получаем последние транзакции
            try {
                List<Transaction> recentTransactions = transactionService.getRecentTransactions(3);
                dashboardData.setRecentTransactions(recentTransactions);
            } catch (Exception e) {
                dashboardData.setRecentTransactions(List.of());
            }

            // Получаем общий баланс
            try {
                BigDecimal totalBalance = accountService.getTotalBalance();
                dashboardData.setTotalBalance(totalBalance);
            } catch (Exception e) {
                dashboardData.setTotalBalance(BigDecimal.ZERO);
            }

            // Проверяем, является ли пользователь админом
            dashboardData.setAdmin(username.equals("admin"));

        } else {
            // Если пользователь не авторизован
            dashboardData.setUsername("Гость");
            dashboardData.setGuest(true);
            dashboardData.setRecentTransactions(List.of());
            dashboardData.setTotalBalance(BigDecimal.ZERO);
            dashboardData.setAdmin(false);
        }

        return dashboardData;
    }

    /**
     * DTO для передачи данных дашборда
     */
    public static class DashboardData {
        private String username;
        private List<Transaction> recentTransactions;
        private BigDecimal totalBalance;
        private boolean isAdmin;
        private boolean isGuest;

        // Геттеры и сеттеры
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public List<Transaction> getRecentTransactions() {
            return recentTransactions;
        }

        public void setRecentTransactions(List<Transaction> recentTransactions) {
            this.recentTransactions = recentTransactions;
        }

        public BigDecimal getTotalBalance() {
            return totalBalance;
        }

        public void setTotalBalance(BigDecimal totalBalance) {
            this.totalBalance = totalBalance;
        }

        public boolean isAdmin() {
            return isAdmin;
        }

        public void setAdmin(boolean admin) {
            isAdmin = admin;
        }

        public boolean isGuest() {
            return isGuest;
        }

        public void setGuest(boolean guest) {
            isGuest = guest;
        }
    }
}
