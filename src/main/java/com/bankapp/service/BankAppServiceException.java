package com.bankapp.service;

public class BankAppServiceException extends Exception{
    public BankAppServiceException(String errorMessage, Throwable err){
        super(errorMessage, err);
    }
}
