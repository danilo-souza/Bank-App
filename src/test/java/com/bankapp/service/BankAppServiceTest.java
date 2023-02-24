package com.bankapp.service;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import com.bankapp.dao.BankAppDao;
import com.bankapp.dao.DaoPersistenceException;
import com.bankapp.dto.BankAccount;
import com.bankapp.dto.CustomerAccount;

import static com.mongodb.client.model.Filters.eq;

public class BankAppServiceTest {
    BankAppService service;

    Document document = new Document();

    @Mock
    private BankAppDao mockDao;

    public BankAppServiceTest() throws DaoPersistenceException{
        MockitoAnnotations.openMocks(this);

        Document doc = new Document();
        doc.append("username", "123");
        doc.append("password", "%Test1234");
        doc.append("customerID", "123");

        doAnswer(new Answer<Void>(){
            public Void answer(InvocationOnMock invocation){
                document = invocation.getArgument(0);
                return null;
            }
        }).when(mockDao).add(any(Document.class));

        when(mockDao.get(any(Document.class))).thenReturn(new Document());
        when(mockDao.update(any(Document.class), any(Document.class))).thenReturn(true);


        service = new BankAppServiceImpl(mockDao, mockDao, mockDao);
    }

    //Create Acount tests
    @Test
    public void testCreateValidAccount(){
        String username = "test";
        String password = "%Test123";

        try{
            CustomerAccount account = service.createAccount(username, password);
            assertNotEquals(account.getPassword(), password);
        } catch(InvalidPasswordException | InvalidUsernameException | BankAppServiceException e){
            fail("Unexpected exception thrown!");
        }
    }

    @Test
    public void testCreateAccountInvalidPassword(){
        //password with too few characters
        String username = "test";
        String password = "%Test12";

        CustomerAccount account;

        try{
            account = service.createAccount(username, password);
            fail("Exception not thrown");
        } catch(InvalidPasswordException e){
            assertNotNull(e);
        } catch(InvalidUsernameException | BankAppServiceException e){
            fail("Wroong exception thrown");
        }

        //No Numbers
        password = "%Tesetststt";
        try{
            account = service.createAccount(username, password);
            fail("Exception not thrown");
        } catch(InvalidPasswordException e){
            assertNotNull(e);
        } catch(InvalidUsernameException | BankAppServiceException e){
            fail("Wroong exception thrown");
        }

        //No special characters
        password = "Test12233";
        try{
            account = service.createAccount(username, password);
            fail("Exception not thrown");
        } catch(InvalidPasswordException e){
            assertNotNull(e);
        } catch(InvalidUsernameException | BankAppServiceException e){
            fail("Wroong exception thrown");
        }

        //No upper case
        password = "#test12233";
        try{
            account = service.createAccount(username, password);
            fail("Exception not thrown");
        } catch(InvalidPasswordException e){
            assertNotNull(e);
        } catch(InvalidUsernameException | BankAppServiceException e){
            fail("Wroong exception thrown");
        }

        //No lower case
        password = "#P124444565656";
        try{
            account = service.createAccount(username, password);
            fail("Exception not thrown");
        } catch(InvalidPasswordException e){
            assertNotNull(e);
        } catch(InvalidUsernameException | BankAppServiceException e){
            fail("Wroong exception thrown");
        }
    }

    @Test
    public void testCreateAccountInvalidUsername() throws DaoPersistenceException{
        String username = "123";
        String password = "%Test1234";

        try{
            service.createAccount(username, password);
            when(mockDao.get(eq("username", username))).thenReturn(document);

            service.createAccount(username, password);
            fail("Exception not thrown");
        } catch(InvalidUsernameException e){
            assertNotNull(e);
        } catch(InvalidPasswordException | BankAppServiceException e){
            fail("Wrong exception thrown");
        }
    }

    //Login tests
    @Test
    public void testLoginSuccess() throws DaoPersistenceException{
        String username = "test";
        String password = "%Test1234";

        try{
            service.createAccount(username, password);
            when(mockDao.get(eq("username", username))).thenReturn(document);

            service.login(username, password);
        } catch(InvalidPasswordException | InvalidUsernameException | BankAppServiceException e){
            fail("Unexpected exception thrown");
        }
    }

    @Test
    public void testLoginFailure() throws DaoPersistenceException{
        String username = "123";
        String password = "%Test1234";

        try{
            service.createAccount(username, password);
            when(mockDao.get(eq("username", username))).thenReturn(document);

            service.login("tes", password);
            fail("Exception not thrown!");
        } catch(InvalidUsernameException e ){
            assertNotNull(e);
        } catch(InvalidPasswordException | BankAppServiceException e){
            fail("Wrong exception thrown");
        }

        try{
            service.login(username, "1234");
            fail("Exception not thrown");
        } catch(InvalidPasswordException e){
            assertNotNull(e);
        } catch(InvalidUsernameException | BankAppServiceException e){
            fail("Wrong exception thrown!");
        }
    }

    //Test open account
    @Test
    public void testOpenCheckings(){
        String token = "123";
        int type = 1;

        try{
            service.openAccount(type, token);
        } catch(AccountOpeningException e){
            fail("Exception thrown!");
        }

        //Try opening a second checkings
        try{
            service.openAccount(type, token);
            fail("Exception not thrown!");
        } catch(AccountOpeningException e){
            assertNotNull(e);
        }
    }

    @Test
    public void testOpenSavings(){
        String token = "123";
        int type = 2;

        try{
            service.openAccount(type, token);
        } catch(AccountOpeningException e){
            fail("Exception thrown!");
        }

        //Try opening a second savings
        try{
            service.openAccount(type, token);
            fail("Exception not thrown!");
        } catch(AccountOpeningException e){
            assertNotNull(e);
        }
    }

    //Test invalid token
    @Test
    public void testOpenAccountInvalid(){
        String token = "#";
        int type = 2;

        try{
            service.openAccount(type, token);
            fail("Exception not thrown!");
        } catch(AccountOpeningException e){
            assertNotNull(e);
        }
    }

    //Test Deposit and Withdraw
    @Test
    public void testDeposit(){
        String token = "123";
        String accountNumber = "123";
        BigDecimal amount = new BigDecimal(100);

        try{
            service.openAccount(1, token);
            int out = service.deposit(amount, token, accountNumber);
            assertEquals(100, out);
        } catch(AccountOpeningException | AccountNotFoundException | BankAppServiceException e ){
            fail("Exception thrown!");
        }

        //Try to deposit negative amount
        amount = new BigDecimal(-100);
        try{
            service.deposit(amount, token, accountNumber);
            fail("Exception not thrown!");
        } catch(BankAppServiceException e ){
            assertNotNull(e);
        } catch(AccountNotFoundException e){
            fail("Wrong exception thrown!");
        }
    }

    @Test
    public void testWithdraw(){
        String token = "123";
        String accountNumber = "123";
        BigDecimal amount = new BigDecimal(100);

        try{
            service.deposit(amount, token, accountNumber);
            int out = service.withdraw(amount, token, accountNumber);
            assertEquals(0, out);
        } catch(AccountNotFoundException | BankAppServiceException e){
            fail("Exception thrown!");
        }

        //Try to withdraw without enough money
        try{
            service.withdraw(amount, token, accountNumber);
            fail("Exception not thrown!");
        } catch(BankAppServiceException e){
            assertNotNull(e);
        } catch(AccountNotFoundException e){
            fail("Wrong exception thrown!");
        }
    }

    //Test transfer
    @Test
    public void testTransfer(){
        String token = "123";
        String accountNumber1 = "123";
        String accountNumber2 = "456";
        BigDecimal amount = new BigDecimal(100);

        try{
            service.deposit(amount, token, accountNumber1);
            int out = service.transfer(amount, token, accountNumber1, accountNumber2);

            assertEquals(0, out);
        } catch(AccountNotFoundException | BankAppServiceException e){
            fail("Exception thrown!");
        }

        //Try to transfer without any money
        try{
            service.transfer(amount, token, accountNumber1, accountNumber2);
        } catch(BankAppServiceException e){
            assertNotNull(e);
        } catch(AccountNotFoundException e){
            fail("Wrong exception thrown!");
        }
    }

    //Test get all accounts
    @Test
    public void testGetAllAccounts(){
        String token = "123";
        List<BankAccount> accounts;

        //Test without any accounts
        try{
            accounts = service.getAccounts(token);

            assertEquals(accounts.size(), 2);
        } catch(BankAppServiceException e){
            fail("Exception thrown!");
        }

        try{
            service.openAccount(1, token);
            service.openAccount(2, token);
            accounts = service.getAccounts(token);

            assertEquals(accounts.size(), 2);
        } catch(BankAppServiceException | AccountOpeningException e){
            fail("Exception thrown!");
        }
    }
}
