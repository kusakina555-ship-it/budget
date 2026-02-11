package ru.boldycheva.mainapp.dto;

import ru.boldycheva.mainapp.entity.Account;

public class AccountEditDataDto {
    private final Account account;
    private final EditAccountDto editAccountDto;
    private final boolean isAdmin;

    public AccountEditDataDto(Account account, EditAccountDto editAccountDto, boolean isAdmin) {
        this.account = account;
        this.editAccountDto = editAccountDto;
        this.isAdmin = isAdmin;
    }

    public Account getAccount() { return account; }
    public EditAccountDto getEditAccountDto() { return editAccountDto; }
    public boolean isAdmin() { return isAdmin; }
}
