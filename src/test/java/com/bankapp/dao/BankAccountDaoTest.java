package com.bankapp.dao;

import org.junit.jupiter.api.Test;

import com.mongodb.client.model.Updates;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.AfterEach;

import static com.mongodb.client.model.Filters.eq;

public class BankAccountDaoTest {
    
    final String db = "BankApp";
    final String collection = "CustomerAccountTest";

    String key = "test";
    String value = "test";

    Bson query = eq(key, value);

    BankAppDao dao;

    public BankAccountDaoTest(){
        dao = new BankAppDaoImpl(db, collection);
    }

    @AfterEach
    public void setUp() throws DaoPersistenceException{
        dao.removeMany(new Document());
    }

    //Tests
    @Test
    public void testAddAndGet(){
        Document doc = new Document();

        doc.append(key, value);

        try{
            dao.add(doc);
            doc = dao.get(query);

            assertEquals(doc.get(key).toString(), key);
        } catch(DaoPersistenceException e){
            fail("Unexpected exception thrown");
        }
    }

    @Test
    public void testRemove(){
        Document doc = new Document();

        doc.append(key, value);

        try{
            dao.add(doc);
            dao.remove(query);
            
            assertNull(dao.get(query));
        } catch(DaoPersistenceException e){
            fail("Unexpected exception thrown");
        }
    }

    @Test
    public void testUpdate(){
        Document doc = new Document();
        doc.append(key, value);

        try{
            dao.add(doc);
            
            Bson updates = Updates.combine(Updates.set("test", "trial"));
            dao.update(query, updates);
            doc = dao.get(eq(key, "trial"));

            assertEquals(doc.get(key).toString(), "trial");
        } catch(DaoPersistenceException e){
            fail("Unexpected exception thrown");
        }
    }

    @Test
    public void testGetAll(){
        Document doc = new Document().append(key, value);
        Document doc2 = new Document().append(key, value);

        try{
            dao.add(doc);
            dao.add(doc2);

            List<Document> docs = dao.getAll(eq(key, value));

            assertEquals(docs.size(), 2);
        } catch(DaoPersistenceException e){
            fail("Unexpected exception thrown!");
        }
    }
}
