package com.retailhr.ems.db;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class EntityManagerFactoryProvider {

    private static final EntityManagerFactory FACTORY = Persistence.createEntityManagerFactory("emsPU");

    private EntityManagerFactoryProvider() {
    }

    public static EntityManagerFactory getFactory() {
        return FACTORY;
    }

    public static void shutdown() {
        if (FACTORY.isOpen()) {
            FACTORY.close();
        }
    }
}