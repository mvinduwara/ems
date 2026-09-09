package com.retailhr.ems.repository;

import com.retailhr.ems.model.entity.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;

public class RoleRepository extends GenericRepository<Role, Integer> {

    public RoleRepository() {
        super(Role.class);
    }

    public Optional<Role> findByName(String roleName) {
        try (EntityManager em = entityManager()) {
            Role role = em.createQuery(
                            "SELECT r FROM Role r WHERE r.roleName = :name", Role.class)
                    .setParameter("name", roleName)
                    .getSingleResult();
            return Optional.of(role);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}