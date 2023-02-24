package com.bankapp.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;
import java.util.Base64.Encoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.Crypt;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bankapp.dao.BankAppDao;
import com.bankapp.dao.DaoPersistenceException;
import com.bankapp.dto.BankAccount;
import com.bankapp.dto.CustomerAccount;

import static com.mongodb.client.model.Filters.eq;

@Component
public class BankAppServiceImpl implements BankAppService{
    BankAppDao customerAccountDao;
    BankAppDao bankAccountDao;
    BankAppDao logDao;

    final String PEPPER = "jhfahfabnnkfabfa";

    @Autowired
    public BankAppServiceImpl(BankAppDao customer, BankAppDao account, BankAppDao log){
        customerAccountDao = customer;
        bankAccountDao = account;
        logDao = log;
    }


    @Override
    public CustomerAccount createAccount(String username, String password)
            throws InvalidPasswordException, InvalidUsernameException, BankAppServiceException {
        
        checkPasswordConstraints(password);

        try{
            if(customerAccountDao.get(eq("username", username)) != null)
                throw new InvalidUsernameException("Username already in use!", null);
        } catch(DaoPersistenceException e){
            throw new InvalidUsernameException("Something went wrong!", e);
        }
        
        String[] temp = hashPassword(password);
        password = temp[0];
        String salt = temp[1];

        CustomerAccount account = new CustomerAccount();
        account.setUsername(username);
        account.setPassword(password);

        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);

        String customerID = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        account.setCustomerID(customerID);

        Document user = new Document();
        user.append("username", username);
        user.append("password", password);
        user.append("customerID", customerID);
        user.append("salt", salt);

        try{
            customerAccountDao.add(user);
            user = new Document();
            user.append("customerID", customerID);
            bankAccountDao.add(user);
        } catch(DaoPersistenceException e){
            throw new BankAppServiceException("Something went wrong! Try again later.", null);
        }

        return account;
    }


    @Override
    public CustomerAccount login(String username, String password) 
            throws InvalidPasswordException, InvalidUsernameException, BankAppServiceException {
        
        Document account = new Document();
        
        try{
            account = customerAccountDao.get(eq("username", username));
        } catch(DaoPersistenceException e){
            throw new BankAppServiceException("Something went wrong try again later!", null);
        }

        if(account == null ||
            !account.containsKey("username") ||
            !account.get("username").toString().equals(username)){
            
            throw new InvalidUsernameException("The username or password is incorrect!", null);
        }

        String dbpw = account.get("password").toString();
        String salt = account.get("salt").toString();
        password = hashPassword(password, salt)[0];

        if(!password.equals(dbpw)){
            throw new InvalidPasswordException("The username or password is incorrect!", null);
        }

        CustomerAccount out = new CustomerAccount();
        out.setPassword(dbpw);
        out.setUsername(username);
        out.setCustomerID(account.get("customerID").toString());

        //TODO: need to give back jwt token

        return out;
    }


    @Override
    public BankAccount openAccount(int type, String token) 
            throws AccountOpeningException{
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'openAccount'");
    }


    @Override
    public int deposit(BigDecimal amount, String token, String accountNumber) 
            throws AccountNotFoundException, BankAppServiceException{
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deposit'");
    }


    @Override
    public int withdraw(BigDecimal amount, String token, String accountNumber) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withdraw'");
    }


    @Override
    public int transfer(BigDecimal amount, String token, String sender, String recepient) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'transfer'");
    }

    @Override
    public List<BankAccount> getAccounts(String token){
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'transfer'");
    }


    private boolean checkPasswordConstraints(String password) throws InvalidPasswordException{
        if(password.length() < 8){
            throw new InvalidPasswordException("Your password must contain more than 8 characters!", null);
        }

        //Got the regex string from:
        //https://www.geeksforgeeks.org/check-if-a-string-contains-uppercase-lowercase-special-characters-and-numeric-values/
        String regex = "^(?=.*[-+_!@#$%^&*.,?])(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);

        if(!m.matches()){
            throw new InvalidPasswordException("Your password must contain a special character, a digit, an upper case," +
                " and a lower case character!", null);
        }

        return true;
    }

    private String[] hashPassword(String password){
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[12];
        random.nextBytes(bytes);
        Encoder encode = Base64.getUrlEncoder().withoutPadding();
        String salt = encode.encodeToString(bytes);

        return new String[]{Crypt.crypt(password + PEPPER, "$5$" + salt), salt};
    }

    private String[] hashPassword(String password, String salt){
        return new String[]{Crypt.crypt(password + PEPPER, "$5$" + salt), salt};
    }
}
