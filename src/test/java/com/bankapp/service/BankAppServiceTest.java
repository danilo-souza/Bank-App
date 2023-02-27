package com.bankapp.service;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.bankapp.dao.BankAppDao;
import com.bankapp.dao.DaoPersistenceException;
import com.bankapp.dto.BankAccount;
import com.bankapp.dto.CustomerAccount;

import static com.mongodb.client.model.Filters.eq;

public class BankAppServiceTest {
    BankAppService service;

    List<Document> documents = new ArrayList<>();

    @Mock
    private BankAppDao mockDao;

    public BankAppServiceTest() throws DaoPersistenceException{
        MockitoAnnotations.openMocks(this);

        doAnswer(new Answer<Void>(){
            public Void answer(InvocationOnMock invocation){
                documents.add(invocation.getArgument(0));
                return null;
            }
        }).when(mockDao).add(any(Document.class));


        service = new BankAppServiceImpl(mockDao, mockDao, mockDao);
    }

    @AfterEach
    public void cleanUp(){
        documents.clear();
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
            when(mockDao.get(eq("username", username))).thenReturn(documents.get(0));

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
            when(mockDao.get(eq("username", username))).thenReturn(documents.get(0));

            String[] out = service.login(username, password);
            assertTrue(!out[0].equals(""));
            assertFalse(out[1].equals(""));
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
            when(mockDao.get(eq("username", username))).thenReturn(documents.get(0));

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
        String username = "test";
        String password = "%Test1234";
        String type = "Checking";

        try{
            service.createAccount(username, password);
            when(mockDao.get(eq("username", username))).thenReturn(documents.get(0));

            String[] out = service.login(username, password);
            service.openAccount(type, out[1], out[0]);
        } catch(AccountOpeningException e){
            fail("Exception thrown!");
        } catch(InvalidPasswordException | 
                BankAppServiceException | 
                DaoPersistenceException |
                InvalidUsernameException e){
            fail("Wrong exception thrown!");
        }

        /*//Try opening a second checkings
        try{
            String[] out = service.login(username, password);
            service.openAccount(type, out[1], out[0]);
            fail("Exception not thrown!");
        } catch(AccountOpeningException e){
            assertNotNull(e);
        } catch(InvalidPasswordException | BankAppServiceException | InvalidUsernameException e){
            fail("Wrong exception thrown!");
        }*/
    }

    @Test
    public void testOpenSavings(){
        String username = "test";
        String password = "%Test1234";
        String type = "Savings";

        try{
            service.createAccount(username, password);
            when(mockDao.get(eq("username", username))).thenReturn(documents.get(0));

            String[] out = service.login(username, password);

            BankAccount result = service.openAccount(type, out[1], out[0]);
            String customerID = result.getCustomerID();

            Document opened = new Document();
            String accountNumber = result.getAccountNumbers().get("Savings");
            opened.append("customerID", customerID);
            opened.append(accountNumber, new Document()
                    .append("type", type)
                    .append("balance", result.getAccounts().get(accountNumber)));

            when(mockDao.get(eq("customerID", result.getCustomerID()))).thenReturn(opened);

            Document ret = mockDao.get(eq("customerID", customerID));

            assertEquals(ret.get(accountNumber).toString(), opened.get(accountNumber).toString());
        } catch(AccountOpeningException e){
            fail("Exception thrown!");
        } catch(InvalidPasswordException | 
                BankAppServiceException | 
                DaoPersistenceException |
                InvalidUsernameException e){
            fail("Wrong exception thrown!");
        }
    }

    //Test invalid token
    @Test
    public void testOpenAccountInvalid(){
        String username = "test";
        String password = "%Test1234";
        String type = "Savings";

        try{
            service.createAccount(username, password);
            when(mockDao.get(eq("username", username))).thenReturn(documents.get(0));

            String[] out = service.login(username, password);
            service.openAccount(type, "agsghas", out[0]);
            fail("Exception not thrown!");
        } catch(AccountOpeningException | BankAppServiceException  e){
            assertNotNull(e);
        } catch(InvalidPasswordException | 
                DaoPersistenceException |
                InvalidUsernameException e){
            fail("Wrong exception thrown!");
        }
    }

    //Test Deposit and Withdraw
    @Test
    public void testDeposit(){
        String username = "test";
        String password = "%Test1234";
        String type = "Savings";
        BigDecimal amount = new BigDecimal(100);
        Document opened = new Document();
        String[] out = new String[2];

        try{
            opened.append("customerID", "1");
            opened.append(type, new Document()
                    .append("accountNumber", "12")
                    .append("balance", 0));
            
            service.createAccount(username, password);
            when(mockDao.get(eq("username", username))).thenReturn(documents.get(0));

            out = service.login(username, password);
            service.openAccount(type, out[1], out[0]);

            when(mockDao.get(eq("customerID", documents.get(0).get("customerID").toString()))).thenReturn(opened);

            BigDecimal delta = service.deposit(amount, out[1], out[0], "12");
            assertEquals(amount, delta);
        } catch(Throwable e ){
            fail("Exception thrown!");
        }

        //Try to deposit negative amount
        amount = new BigDecimal(-200);
        try{
            service.deposit(amount, out[1], out[0], "12");
            fail("Exception not thrown!");
        } catch(BankAppServiceException e ){
            assertNotNull(e);
        } catch(AccountNotFoundException e){
            fail("Wrong exception thrown!");
        }
    }

    //Test Deposit
    @Test
    public void testWithdraw(){
        String username = "test";
        String password = "%Test1234";
        String type = "Savings";
        BigDecimal amount = new BigDecimal(100);
        Document opened = new Document();
        String[] out = new String[2];

        try{
            opened.append("customerID", "1");
            opened.append(type, new Document()
                    .append("accountNumber", "12")
                    .append("balance", 100));
            
            service.createAccount(username, password);
            when(mockDao.get(eq("username", username))).thenReturn(documents.get(0));

            out = service.login(username, password);
            service.openAccount(type, out[1], out[0]);

            when(mockDao.get(eq("customerID", documents.get(0).get("customerID").toString()))).thenReturn(opened);

            BigDecimal delta = service.withdraw(amount, out[1], out[0], "12");
            assertEquals(BigDecimal.ZERO, delta);

            opened.replace(type, new Document()
                .append("accountNumber", "12")
                .append("balance", delta.toString()));
        } catch(Throwable e ){
            fail("Exception thrown!");
        }

        //Try to withdraw without money
        try{
            service.withdraw(amount, out[1], out[0], "12");
            fail("Exception not thrown!");
        } catch(BankAppServiceException e ){
            assertNotNull(e);
        } catch(AccountNotFoundException e){
            fail("Wrong exception thrown!");
        }
    }

    //Test transfer
    @Test
    public void testTransfer() {
        String username = "test";
        String password = "%Test1234";

        String accountNumber1 = "123";
        String accountNumber2 = "456";
        BigDecimal amount = new BigDecimal(100);

        String[] out = new String[2];

        Document account1 = new Document()
                .append("customerID", "1")
                .append("Checking", new Document()  
                    .append("accountNumber", accountNumber1)
                    .append("balance", "100"));

            Document account2 = new Document()
                .append("customerID", "1")
                .append("Savings", new Document()  
                    .append("accountNumber", accountNumber2)
                    .append("balance", "0"));

        try{
            service.createAccount(username, password);

            documents.get(0).replace("customerID", "1");
            when(mockDao.get(eq("username", username))).thenReturn(documents.get(0));
            
            out = service.login(username, password);

            when(mockDao.get(eq("customerID", "1"))).thenReturn(account1);
            when(mockDao.get(eq("Savings.accountNumber", "456"))).thenReturn(account2);


            BigDecimal balance = service.transfer(amount, out[1], out[0], accountNumber1, accountNumber2, "Checking", "Savings");

            assertEquals(BigDecimal.ZERO, balance);
        } catch(AccountNotFoundException | BankAppServiceException |
                InvalidPasswordException | InvalidUsernameException | DaoPersistenceException e){
            fail("Exception thrown!");
        }

        //Transfer without money
        try{
            account1.replace("Checking", new Document().append("accountNumber", accountNumber1).append("balance", "0"));

            service.transfer(amount, out[1], out[0], accountNumber1, accountNumber2, "Checking", "Savings");

            fail("Exception not thrown");
        } catch(AccountNotFoundException | BankAppServiceException e){
            assertNotNull(e);
        }
    }

    //Test get all accounts
    @Test
    public void testGetAllAccounts(){
        String username = "test";
        String password = "%Test1234";
        BankAccount account = new BankAccount();

        String[] out = new String[2];

        Document account1 = new Document()
                .append("customerID", "1")
                .append("Checking", new Document()  
                    .append("accountNumber", "123")
                    .append("balance", "100"))
                .append("Savings", new Document()  
                    .append("accountNumber", "123")
                    .append("balance", "100"));

        //Test without any accounts
        try{
            service.createAccount(username, password);

            documents.get(0).replace("customerID", "1");
            when(mockDao.get(eq("username", username))).thenReturn(documents.get(0));

            out = service.login(username, password);

            account = service.getAccounts(out[1], out[0]);

            fail("Exception not thrown!");
        } catch(BankAppServiceException | InvalidUsernameException | 
                InvalidPasswordException | DaoPersistenceException | AccountNotFoundException e){
            assertNotNull(e);
        }

        try{
            /*Map<String, String> acNumbers = new HashMap<>();
            acNumbers.put("Checking", "123");

            Map<String, BigDecimal> acBalance = new HashMap<>();
            acBalance.put("123", BigDecimal.ZERO);

            BankAccount ac1 = new BankAccount();
            ac1.setCustomerID("1");
            ac1.setAccountNumbers(acNumbers);
            ac1.setAccounts(acBalance);
            
            accounts.add(ac1);
            accounts.add(ac1);*/


            when(mockDao.get(eq("customerID", "1"))).thenReturn(account1);

            account = service.getAccounts(out[1], out[0]);

            assertEquals(account.getAccountNumbers().size(), 2);
        } catch(BankAppServiceException | AccountNotFoundException | DaoPersistenceException e){
            fail("Exception thrown!");
        }
    }
}
