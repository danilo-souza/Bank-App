package com.bankapp.service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.Base64.Encoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.Crypt;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.bankapp.dao.BankAppDao;
import com.bankapp.dao.DaoPersistenceException;
import com.bankapp.dto.BankAccount;
import com.bankapp.dto.CustomerAccount;

import jakarta.xml.bind.DatatypeConverter;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.model.Updates;

public class BankAppServiceImpl implements BankAppService{
    BankAppDao customerAccountDao;
    BankAppDao bankAccountDao;
    BankAppDao logDao;

    final transient String PEPPER = "jhfahfabnnkfabfa";
    final transient byte[] keyHMAC = "12345".getBytes();

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
    public String[] login(String username, String password) 
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

        /*CustomerAccount out = new CustomerAccount();
        out.setPassword(dbpw);
        out.setUsername(username);
        out.setCustomerID(account.get("customerID").toString());*/

        //Set JWT token
        String[] out = createJWT(username);


        return out;
    }


    @Override
    public BankAccount openAccount(String type, String token, String fingerprint) 
            throws AccountOpeningException, BankAppServiceException{
        
        //token = new String(Base64.getUrlDecoder().decode(token));
        JSONObject payload = decodeJWT(verifyJWT(fingerprint, token));
       
        SecureRandom random = new SecureRandom();
        String accountNumber = String.valueOf(random.nextLong());

        Document account = new Document();
        account.append("accountNumber", accountNumber);
        account.append("balance", 0);

        BankAccount out = new BankAccount();
        Map<String, BigDecimal> accounts = new HashMap<>();
        Map<String, String> accountNumbers = new HashMap<>();

        accounts.put(accountNumber, BigDecimal.ZERO);
        accountNumbers.put(type, accountNumber);
        
        try{
            Document result = customerAccountDao.get(eq("username", payload.getString("username")));
            String customerID = result.get("customerID").toString();
            out.setCustomerID(customerID);


            bankAccountDao.update(eq("customerID", customerID), 
                                    Updates.combine(Updates.setOnInsert(type, account.toJson())));
        } catch(DaoPersistenceException e){
            throw new AccountOpeningException("There was a problem opening the account!", null);
        }

        out.setAccounts(accounts);
        out.setAccountNumbers(accountNumbers);
        return out;
    }


    @Override
    @Transactional
    public BigDecimal deposit(BigDecimal amount, String token, String fingerprint, String accountNumber) 
            throws AccountNotFoundException, BankAppServiceException{

        if(amount.compareTo(BigDecimal.ZERO) == -1){
            throw new BankAppServiceException("Invalid deposit amount!", null);
        }
        
        JSONObject payload = decodeJWT(verifyJWT(fingerprint, token));
        JSONObject account = new JSONObject();

        Document customer = new Document();
        String customerID = "";

        try{
            customer = customerAccountDao.get(eq("username", payload.getString("username")));
            customerID = customer.get("customerID").toString();
            account = new JSONObject(bankAccountDao.get(eq("customerID", customerID)).toJson());
        } catch(DaoPersistenceException e){
            throw new AccountNotFoundException("Could not locate account!", null);
        }

        String type = "";
        JSONObject inner = new JSONObject();
        try{
            inner = account.getJSONObject("Checking");
        } catch(JSONException e){
            inner = null;
        }
        if(inner != null && inner.getString("accountNumber").equals(accountNumber)){
            type = "Checking";
            account = account.getJSONObject(type);
        }
        else{
            type = "Savings";
            try{
                inner = account.getJSONObject("Savings");
            } catch(JSONException e){
                throw new BankAppServiceException("Invalid account number!", null);
            }

            if(inner.getString("accountNumber").equals(accountNumber)) 
                account = account.getJSONObject(type);
            else
                throw new BankAppServiceException("Invalid account number!", null);
        }

        BigDecimal balance = new BigDecimal(account.get("balance").toString());
        
        balance = balance.add(amount);

        try{
            Bson update = Updates.combine(Updates.set(type + ".balance", balance));

            bankAccountDao.update(
                eq("customerID", customerID), 
                update);
        } catch(DaoPersistenceException e){
            throw new BankAppServiceException("Something went wrong!", null);
        }

        logTransactions(accountNumber, amount.toString() + " Deposited");

        return balance;
    }


    @Override
    @Transactional
    public BigDecimal withdraw(BigDecimal amount, String token, String fingerprint, String accountNumber) 
        throws AccountNotFoundException, BankAppServiceException{

        amount = amount.abs();

        BigDecimal balance = BigDecimal.ZERO;

        JSONObject payload = decodeJWT(verifyJWT(fingerprint, token));
        JSONObject account;

        String customerID = "";
        Document customer = new Document();


        try{
            customer = customerAccountDao.get(eq("username", payload.getString("username")));
            customerID = customer.get("customerID").toString();
            account = new JSONObject(bankAccountDao.get(eq("customerID", customerID)).toJson());
        } catch(DaoPersistenceException e){
            throw new AccountNotFoundException("Could not locate account!", null);
        }

        String type = "";
        JSONObject inner = new JSONObject();
        try{
            inner = account.getJSONObject("Checking");
        } catch(JSONException e){
            inner = null;
        }
        if(inner != null && inner.getString("accountNumber").equals(accountNumber)){
            account = account.getJSONObject(type);
            type = "Checking";
        }
        else{
            type = "Savings";
            try{
                inner = account.getJSONObject("Savings");
            } catch(JSONException e){
                throw new BankAppServiceException("Invalid account number!", null);
            }

            if(inner.getString("accountNumber").equals(accountNumber)) 
                account = account.getJSONObject(type);
            else
                throw new BankAppServiceException("Invalid account number!", null);
        }

        balance = new BigDecimal(account.get("balance").toString());
        
        if(balance.compareTo(amount) == -1)
            throw new BankAppServiceException("Not enough funds!", null);
            
        balance = balance.subtract(amount);

        try{
            Bson update = Updates.combine(Updates.set(type + ".balance", balance));

            bankAccountDao.update(
                eq("customerID", customerID), 
                update);
        } catch(DaoPersistenceException e){
            throw new BankAppServiceException("Something went wrong!", null);
        }

        logTransactions(accountNumber, amount.toString() + " Withdrawn");

        return balance;
    }


    @Override
    @Transactional
    public BigDecimal transfer(BigDecimal amount, String token, 
        String fingerprint, String sender, String recepient, String senderType, String recepientType)
        throws AccountNotFoundException, BankAppServiceException {
        
        JSONObject payload = decodeJWT(verifyJWT(fingerprint, token));
        JSONObject account1;
        JSONObject account2;

        try{
            Document temp = bankAccountDao.get(eq("username", payload.getString("username")));
            temp = bankAccountDao.get(eq("customerID", temp.get("customerID").toString()));
            account1 = new JSONObject(temp.toJson());

            temp = bankAccountDao.get(eq(recepientType + ".accountNumber", recepient));
            account2 = new JSONObject(temp.toJson());
        } catch(DaoPersistenceException e){
            throw new AccountNotFoundException("There was a problem locating the account!", null);
        }

        BigDecimal senderBalance = new BigDecimal(account1.getJSONObject(senderType).getString("balance"));
        BigDecimal recepientBalance = new BigDecimal(account2.getJSONObject(recepientType).getString("balance"));

        if(senderBalance.compareTo(amount) == -1)
            throw new BankAppServiceException("Not enough funds to comple the transaction", null);

        senderBalance = senderBalance.subtract(amount);
        recepientBalance = recepientBalance.add(amount);


        //Update the database
        try{
            Bson update = Updates.combine(Updates.set(senderType + ".balance", senderBalance.toString()));
            Bson query = eq("customerID", account1.getString("customerID"));
            bankAccountDao.update(query, update);

            update = Updates.combine(Updates.set(recepientType + ".balance", recepientBalance.toString()));
            query = eq("customerID", account2.getString("customerID"));
            bankAccountDao.update(query, update);
        } catch(DaoPersistenceException e){
            throw new AccountNotFoundException("Could Not Complete the transaction!", null);
        }

        logTransactions(sender, amount.toString() + " Transfered To " + recepient);

        return senderBalance;
    }

    @Override
    public BankAccount getAccounts(String token, String fingerprint)
        throws AccountNotFoundException, BankAppServiceException{

        Document doc = new Document();

        JSONObject payload = decodeJWT(verifyJWT(fingerprint, token));
        String username = payload.getString("username");

        try{
            Document temp = customerAccountDao.get(eq("username", username));
            String customerID = temp.getString("customerID");

            doc = bankAccountDao.get(eq("customerID", customerID));

        } catch(DaoPersistenceException e){
            throw new AccountNotFoundException("Could not find the account!", null);
        }

        if(doc == null)
            throw new BankAppServiceException("Could not find profile", null);

        BankAccount temp = new BankAccount();
        Map<String, String> accountNumber = new HashMap<>();
        Map<String, BigDecimal> accountBalance = new HashMap<>();

        Document account = doc.get("Checking", Document.class);
        
        if(account != null){
            accountNumber.put("Checking", account.getString("accountNumber"));
            accountBalance.put(account.getString("accountNumber"), 
                                new BigDecimal(account.getString("balance")));
        }

        account = doc.get("Savings", Document.class);
        if(account != null){
            accountNumber.put("Savings", account.getString("accountNumber"));
            accountBalance.put(account.getString("accountNumber"),
                                new BigDecimal(account.getString("balance")));
        }


        
        temp.setCustomerID(doc.getString("customerID"));
        temp.setAccountNumbers(accountNumber);
        temp.setAccounts(accountBalance);

        return temp;
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

    //Got it from https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html
    private String[] createJWT(String username) throws BankAppServiceException{
        SecureRandom random = new SecureRandom();
        byte[] randomFgp = new byte[50];
        random.nextBytes(randomFgp);

        String fingerprint = DatatypeConverter.printHexBinary(randomFgp);
        
        MessageDigest sha;
        byte[] fingerprintDigest;

        try{
            sha = MessageDigest.getInstance("SHA-256");
            fingerprintDigest = sha.digest(fingerprint.getBytes("utf-8"));
        } catch(NoSuchAlgorithmException | UnsupportedEncodingException e){
            throw new BankAppServiceException("There was a problem logging you in!", null);
        }

        String fingerprintHash = DatatypeConverter.printHexBinary(fingerprintDigest);

        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.MINUTE, 15);
        Date expiration = cal.getTime();
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);

        String token = JWT.create().withSubject("login")
            .withExpiresAt(expiration)
            .withIssuer("BankApp")
            .withIssuedAt(now)
            .withNotBefore(now)
            .withClaim("fingerprint", fingerprintHash)
            .withHeader(header)
            .withPayload(payload)
            .sign(Algorithm.HMAC256(this.keyHMAC));

        return new String[]{fingerprint, token};
    }

    private String verifyJWT(String fingerprint, String token) throws BankAppServiceException{
        //String fingerprint = null;

        /*if(request.getCookies() != null && request.getCookies().length > 0){
            List<Cookie> cookies = Arrays.stream(request.getCookies()).collect(Collectors.toList());
            Optional<Cookie> cookie = cookies.stream().filter(c -> "__Secure-Fgp"
                                                        .equals(c.getName())).findFirst();
            
            if(cookie.isPresent()){
                fingerprint = cookie.get().getValue();
            }
        }*/

        MessageDigest sha;
        byte[] fingerprintDigest;

        try{
            sha = MessageDigest.getInstance("SHA-256");
            fingerprintDigest = sha.digest(fingerprint.getBytes("utf-8"));
        } catch(NoSuchAlgorithmException | UnsupportedEncodingException e){
            throw new BankAppServiceException("There was a problem logging you in!", null);
        }

        String fingerprintHash = DatatypeConverter.printHexBinary(fingerprintDigest);

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(keyHMAC))
                                    .withIssuer("BankApp")
                                    .withClaim("fingerprint", fingerprintHash)
                                    .build();
        
        //String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        try{
            DecodedJWT decoded = verifier.verify(token);
            return decoded.getPayload();
        } catch(Throwable e){
            throw new BankAppServiceException("There was a problem authenticating you!", null);
        }
    }

    private JSONObject decodeJWT(String token){
        return new JSONObject(new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8));
    }

    private void logTransactions(String accountNumber, String log) throws BankAppServiceException{
        try{
            logDao.add(new Document().append(accountNumber, log));
        }
        catch(DaoPersistenceException e){
            throw new BankAppServiceException("There was a problem logging the transaction!", null);
        }
    }
}
