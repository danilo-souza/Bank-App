package com.bankapp.dao;

import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

public interface BankAppDao {
    public void add(Document doc)
        throws DaoPersistenceException;
    
    public void remove(Bson query)
        throws DaoPersistenceException;

    public Document get(Bson query)
        throws DaoPersistenceException;

    public boolean update(Bson query, Bson update)
        throws DaoPersistenceException;

    public void removeMany(Bson query)
        throws DaoPersistenceException;

    public List<Document>getAll(Bson query)
        throws DaoPersistenceException;
}