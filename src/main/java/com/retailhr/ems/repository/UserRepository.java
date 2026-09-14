package com.retailhr.ems.repository;

import com.retailhr.ems.model.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;

public class UserRepository extends GenericRepository<User, Integer> {

    public UserRepository() {
        super(User.class);
    }

    public Optional<User> findByUsername(String username) {
        try (EntityManager em = entityManager()) {
            User user = em.createQuery(
                            "SELECT u FROM User u JOIN FETCH u.role WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    public boolean existsByUsername(String username) {
        try (EntityManager em = entityManager()) {
            Long count = em.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return count > 0;
        }
    }
}