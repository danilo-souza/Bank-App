package com.bankapp.service;

import java.math.BigDecimal;

import com.bankapp.dto.BankAccount;
import com.bankapp.dto.CustomerAccount;

public interface BankAppService {
    public CustomerAccount createAccount(String username, String password)
        throws InvalidPasswordException, InvalidUsernameException, BankAppServiceException;

    public String[] login(String username, String password)
        throws InvalidPasswordException, InvalidUsernameException, BankAppServiceException;

    public BankAccount openAccount(String type, String token, String fingerprint)
        throws AccountOpeningException, BankAppServiceException;

    public BigDecimal deposit(BigDecimal amount, String token, String fingerprint, String accountNumber)
        throws AccountNotFoundException, BankAppServiceException;

    public BigDecimal withdraw(BigDecimal amount, String token, String fingerprint, String accountNumber)
        throws AccountNotFoundException, BankAppServiceException;

    public BigDecimal transfer(BigDecimal amount, String token, String fingerprint, String sender, String recepient, 
        String senderType, String recepientType)
        throws AccountNotFoundException, BankAppServiceException;

    public BankAccount getAccounts(String token, String fingerprint)
        throws AccountNotFoundException, BankAppServiceException;
}
