package io.github.hurelhuyag.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface ExtendedJpaRepository<Entity, Id> extends JpaRepositoryImplementation<Entity, Id> {

    /**
     * @see jakarta.persistence.EntityManager#persist(Object)
     */
    Entity persist(Entity entity);

    Page<Entity> findAll(List<Criteria> filters, String entityGraph, Pageable pageable);
}
