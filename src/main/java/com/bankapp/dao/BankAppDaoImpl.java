package com.bankapp.dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

import com.bankapp.dto.CustomerAccount;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class BankAppDaoImpl implements BankAppDao{
    CustomerAccount account;

    private MongoCollection<Document> collection;

    public BankAppDaoImpl(String db, String collection){
        account = new CustomerAccount();

        //Connect to an instance running on localhost port 27017
        MongoClient mongo = MongoClients.create();

        MongoDatabase data = mongo.getDatabase(db);

        this.collection = data.getCollection(collection);
    }

    @Override
    public void add(Document doc) throws DaoPersistenceException {
        try{
            collection.insertOne(doc);
        } catch(Throwable e){
            throw new DaoPersistenceException("Something went wrong; could not add data to the database!", null);
        }
    }

    @Override
    public void remove(Bson query) throws DaoPersistenceException {
        try{
            collection.deleteOne(query);
        } catch(Throwable e){
            throw new DaoPersistenceException("Something went wrong; could not remove data form the database!", null);
        }
    }

    @Override
    public Document get(Bson query) throws DaoPersistenceException {
        try{
            Document doc = collection.find(query).first();

            return doc;
        } catch(Throwable e){
            throw new DaoPersistenceException("Something went wrong; there was a problem getiing the data", null);
        }
    }

    @Override
    public boolean update(Bson query, Bson update) throws DaoPersistenceException {
        try{
            return collection.updateOne(query, update).wasAcknowledged();
        } catch(Throwable e){
            throw new DaoPersistenceException("Something went wrong updating the data!", null);
        }
    }

    @Override
    public void removeMany(Bson query) throws DaoPersistenceException{
        try{
            collection.deleteMany(query);
        } catch(Throwable e){
            throw new DaoPersistenceException("Something went wrong; could not remove data form the database!", null);
        }
    }

    @Override
    public List<Document> getAll(Bson query) throws DaoPersistenceException{
        try{
            List<Document> out = new ArrayList<>();
            MongoCursor<Document> iterator = collection.find(query).cursor();
            while(iterator != null && iterator.hasNext()){
                out.add(iterator.next());
            }
            
            return out;
        } catch(Throwable e){
            throw new DaoPersistenceException("Something went wrong; could not complete query!", null);
        }
    }
    
}
