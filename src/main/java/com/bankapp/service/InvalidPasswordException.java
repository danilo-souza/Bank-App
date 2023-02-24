package com.bankapp.service;

public class InvalidPasswordException extends Exception{
    public InvalidPasswordException(String errorMessage, Throwable err){
        super(errorMessage, err);
    }
}
