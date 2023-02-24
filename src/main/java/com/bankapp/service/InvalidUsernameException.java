package com.bankapp.service;

public class InvalidUsernameException extends Exception{
    public InvalidUsernameException(String errorMessage, Throwable err){
        super(errorMessage, err);
    }
}
