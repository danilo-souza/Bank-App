package com.bankapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

//TODO: Set parameters for accountNumber generation
//TODO: Set parameters for salt generation
//TODO: Set up security configurations (redirecting to https)
//TODO: Set up CORS
//TODO: Set up transactions for mongo

@SpringBootApplication
@ImportResource("classpath:applicationContext.xml")
public class BankappApplication {

	public static void main(String[] args) {		
		SpringApplication.run(BankappApplication.class, args);
	}

}
