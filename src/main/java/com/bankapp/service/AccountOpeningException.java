package com.bankapp.service;

public class AccountOpeningException extends Exception{
    public AccountOpeningException(String errorMessage, Throwable err){
        super(errorMessage, err);
    }
}
