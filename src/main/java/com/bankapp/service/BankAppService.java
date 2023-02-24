package com.bankapp.service;

import java.math.BigDecimal;
import java.util.List;

import com.bankapp.dto.BankAccount;
import com.bankapp.dto.CustomerAccount;

public interface BankAppService {
    public CustomerAccount createAccount(String username, String password)
        throws InvalidPasswordException, InvalidUsernameException, BankAppServiceException;

    public CustomerAccount login(String username, String password)
        throws InvalidPasswordException, InvalidUsernameException, BankAppServiceException;

    public BankAccount openAccount(int type, String token)
        throws AccountOpeningException;

    public int deposit(BigDecimal amount, String token, String accountNumber)
        throws AccountNotFoundException, BankAppServiceException;

    public int withdraw(BigDecimal amount, String token, String accountNumber)
        throws AccountNotFoundException, BankAppServiceException;

    public int transfer(BigDecimal amount, String token, String sender, String recepient)
        throws AccountNotFoundException, BankAppServiceException;

    public List<BankAccount> getAccounts(String token)
        throws BankAppServiceException;
}
