package com.bankapp.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;

import com.bankapp.dto.BankAccount;
import com.bankapp.dto.CustomerAccount;
import com.bankapp.service.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class BankAppController {
    BankAppService service;
    
    @Autowired
    public BankAppController(BankAppService service){
        this.service = service;
    }

    @PostMapping("/createAccount")
    private ResponseEntity<CustomerAccount> createAccount(@RequestBody CustomerAccount customerAccount){

        try{
            customerAccount = service.createAccount(customerAccount.getUsername(), customerAccount.getPassword());
            return ResponseEntity.ok(customerAccount);
        } catch(InvalidPasswordException |
                InvalidUsernameException |
                BankAppServiceException e){
            
            return new ResponseEntity<CustomerAccount>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    private ResponseEntity<String> login(@RequestBody CustomerAccount customerAccount, HttpServletResponse response){
        String username = customerAccount.getUsername();
        String password = customerAccount.getPassword();

        String[] out = new String[2];

        try{
            out = service.login(username, password);
        } catch(InvalidPasswordException |
                InvalidUsernameException |
                BankAppServiceException e){
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
        
        String fingerprintCookie = "__Secure-Fgp=" + out[0]
                           + "; SameSite=Strict; HttpOnly; Secure";
        response.addHeader("Set-Cookie", fingerprintCookie);
        response.addHeader("Set-Cookie", out[1]);

        return ResponseEntity.ok("Login successsfull");
    }

    @PostMapping("/deposit/{amount}")
    private ResponseEntity<BigDecimal> deposit(@PathVariable String amount, @RequestBody String accountNumber, HttpServletRequest request){
        try{
            BigDecimal dep = new BigDecimal(amount);
            List<Cookie> cookies = Arrays.stream(request.getCookies()).collect(Collectors.toList());

            Optional<Cookie> fingerprint = cookies.stream().filter(c -> "__Secure-Fgp".equals(c.getName())).findFirst();
            Optional<Cookie> token = cookies.stream().filter(c -> "JWT".equals(c.getName())).findFirst();

            BigDecimal delta = service.deposit(dep, token.get().getValue(), fingerprint.get().getValue(), accountNumber);

            return ResponseEntity.ok(delta);
        } catch(Throwable e){
            return new ResponseEntity<BigDecimal>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/transfer/{amount}")
    private ResponseEntity<BigDecimal> transfer(@PathVariable String amount, @RequestBody String accountNumber1,
            @RequestBody String accountNumber2, @RequestBody String senderType, @RequestBody String recepientType, 
            HttpServletRequest request){

        try{
            BigDecimal delta = new BigDecimal(amount);
            List<Cookie> cookies = Arrays.stream(request.getCookies()).collect(Collectors.toList());

            Optional<Cookie> fingerprint = cookies.stream().filter(c -> "__Secure-Fgp".equals(c.getName())).findFirst();
            Optional<Cookie> token = cookies.stream().filter(c -> "JWT".equals(c.getName())).findFirst();

            delta = service.transfer(delta, token.get().getValue(), fingerprint.get().getValue(), 
                                    accountNumber1, accountNumber2, senderType, recepientType);

            return ResponseEntity.ok(delta);
        } catch(Throwable e){
            return new ResponseEntity<BigDecimal>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/accounts")
    private ResponseEntity<BankAccount> getAccounts(HttpServletRequest request){
        try{
            List<Cookie> cookies = Arrays.stream(request.getCookies()).collect(Collectors.toList());

            Optional<Cookie> fingerprint = cookies.stream().filter(c -> "__Secure-Fgp".equals(c.getName())).findFirst();
            Optional<Cookie> token = cookies.stream().filter(c -> "JWT".equals(c.getName())).findFirst();

            BankAccount account = service.getAccounts(token.get().getValue(), fingerprint.get().getValue());

            return ResponseEntity.ok(account);
        } catch(Throwable e){
            return new ResponseEntity<BankAccount>(HttpStatus.BAD_REQUEST);
        }
    }
}
