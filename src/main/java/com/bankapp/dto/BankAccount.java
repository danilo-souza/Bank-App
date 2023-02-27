package com.bankapp.dto;
import java.math.BigDecimal;
import java.util.Map;

import lombok.Data;

@Data
public class BankAccount {
    private String customerID;
    private Map<String, BigDecimal> accounts;
    private Map<String, String> accountNumbers;
}
