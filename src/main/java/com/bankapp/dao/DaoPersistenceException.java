package com.bankapp.dao;

public class DaoPersistenceException extends Exception{
    public DaoPersistenceException(String errorMessage, Throwable error){
        super(errorMessage, error);
    }
}
