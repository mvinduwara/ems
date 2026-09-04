package com.retailhr.ems.repository;

import com.retailhr.ems.db.EntityManagerFactoryProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class GenericRepository<T, ID> {

    private final Class<T> entityClass;

    protected GenericRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected EntityManager entityManager() {
        return EntityManagerFactoryProvider.getFactory().createEntityManager();
    }

    public T save(T entity) {
        return executeInTransaction(em -> {
            em.persist(entity);
            return entity;
        });
    }

    public T update(T entity) {
        return executeInTransaction(em -> em.merge(entity));
    }

    public Optional<T> findById(ID id) {
        try (EntityManager em = entityManager()) {
            return Optional.ofNullable(em.find(entityClass, id));
        }
    }

    public List<T> findAll() {
        try (EntityManager em = entityManager()) {
            return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                    .getResultList();
        }
    }

    public void delete(T entity) {
        executeInTransaction(em -> {
            T managed = em.contains(entity) ? entity : em.merge(entity);
            em.remove(managed);
            return null;
        });
    }

    public void deleteById(ID id) {
        executeInTransaction(em -> {
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
            return null;
        });
    }

    protected <R> R executeInTransaction(Function<EntityManager, R> action) {
        EntityManager em = entityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R result = action.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }
}