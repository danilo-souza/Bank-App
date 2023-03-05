package com.bankapp.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.Document;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.bankapp.dto.BankAccount;
import com.bankapp.dto.CustomerAccount;
import com.bankapp.service.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true", exposedHeaders = {"Set-Cookie"})
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
        
        //TODO: Change SameSite back to strict
        String fingerprintCookie = "__Secure-Fgp=" + out[0]
                           + "; SameSite=None; HttpOnly; Secure";
        response.addHeader("Set-Cookie", fingerprintCookie);
        response.addHeader("Set-Cookie", "bankapp-jwt=" + out[1] + "; SameSite=None; Secure");
        response.setStatus(HttpStatus.OK.value());

        return ResponseEntity.ok("Login successsfull");
    }

    @PutMapping("/openAccount")
    private ResponseEntity<BankAccount> openAccount(@RequestBody Map<String, String> body, HttpServletRequest request){
        try{
            List<Cookie> cookies = Arrays.stream(request.getCookies()).collect(Collectors.toList());

            Optional<Cookie> fingerprint = cookies.stream().filter(c -> "__Secure-Fgp".equals(c.getName())).findFirst();
            Optional<Cookie> token = cookies.stream().filter(c -> "bankapp-jwt".equals(c.getName())).findFirst();

            String type = body.get("type");

            BankAccount account = service.openAccount(type, token.get().getValue(), fingerprint.get().getValue());

            return ResponseEntity.ok(account);
        } catch(Throwable e){
            return new ResponseEntity<BankAccount>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/deposit/{amount}")
    private ResponseEntity<BigDecimal> deposit(@PathVariable String amount, @RequestBody Map<String, String> accountMap, HttpServletRequest request){
        try{
            BigDecimal dep = new BigDecimal(amount);
            List<Cookie> cookies = Arrays.stream(request.getCookies()).collect(Collectors.toList());

            Optional<Cookie> fingerprint = cookies.stream().filter(c -> "__Secure-Fgp".equals(c.getName())).findFirst();
            Optional<Cookie> token = cookies.stream().filter(c -> "bankapp-jwt".equals(c.getName())).findFirst();

            String accountNumber = accountMap.get("number");

            BigDecimal delta = service.deposit(dep, token.get().getValue(), fingerprint.get().getValue(), accountNumber);

            return ResponseEntity.ok(delta);
        } catch(Throwable e){
            return new ResponseEntity<BigDecimal>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/withdraw/{amount}")
    private ResponseEntity<BigDecimal> withdraw(@PathVariable String amount, @RequestBody Map<String, String> accountMap, HttpServletRequest request){
        try{
            BigDecimal with = new BigDecimal(amount);
            List<Cookie> cookies = Arrays.stream(request.getCookies()).collect(Collectors.toList());

            Optional<Cookie> fingerprint = cookies.stream().filter(c -> "__Secure-Fgp".equals(c.getName())).findFirst();
            Optional<Cookie> token = cookies.stream().filter(c -> "bankapp-jwt".equals(c.getName())).findFirst();

            String accountNumber = accountMap.get("number");

            BigDecimal delta = service.withdraw(with, token.get().getValue(), fingerprint.get().getValue(), accountNumber);

            return ResponseEntity.ok(delta);
        } catch(Throwable e){
            return new ResponseEntity<BigDecimal>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/transfer/{amount}")
    private ResponseEntity<BigDecimal> transfer(@PathVariable String amount, @RequestBody Map<String, String> transfer, HttpServletRequest request){
        String accountNumber1 = transfer.get("accountNumber1");
        String accountNumber2 = transfer.get("accountNumber2");
        String senderType = transfer.get("senderType");
        String recepientType = transfer.get("recepientType");


        try{
            BigDecimal delta = new BigDecimal(amount);
            List<Cookie> cookies = Arrays.stream(request.getCookies()).collect(Collectors.toList());

            Optional<Cookie> fingerprint = cookies.stream().filter(c -> "__Secure-Fgp".equals(c.getName())).findFirst();
            Optional<Cookie> token = cookies.stream().filter(c -> "bankapp-jwt".equals(c.getName())).findFirst();

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
            Optional<Cookie> token = cookies.stream().filter(c -> "bankapp-jwt".equals(c.getName())).findFirst();

            BankAccount account = service.getAccounts(token.get().getValue(), fingerprint.get().getValue());

            return ResponseEntity.ok(account);
        } catch(Throwable e){
            return new ResponseEntity<BankAccount>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/logs")
    private ResponseEntity<List<Document>> getLogs(HttpServletRequest request){
        try{
            List<Cookie> cookies = Arrays.stream(request.getCookies()).collect(Collectors.toList());

            Optional<Cookie> fingerprint = cookies.stream().filter(c -> "__Secure-Fgp".equals(c.getName())).findFirst();
            Optional<Cookie> token = cookies.stream().filter(c -> "bankapp-jwt".equals(c.getName())).findFirst();

            List<Document> out = service.getLogs(token.get().getValue(), fingerprint.get().getValue());

            return  ResponseEntity.ok(out);
        } catch(Throwable e){
            return new ResponseEntity<List<Document>>(HttpStatus.BAD_REQUEST);
        }
    }
}
