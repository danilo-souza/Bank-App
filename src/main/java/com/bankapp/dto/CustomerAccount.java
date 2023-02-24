package com.bankapp.dto;

import lombok.Data;

@Data
public class CustomerAccount {
    private String username;
    private String password;
    private String customerID;
}