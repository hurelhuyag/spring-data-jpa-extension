package io.github.hurelhuyag.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface ExtendedJpaRepository<Entity, Id> extends JpaRepositoryImplementation<Entity, Id> {

    /**
     * @see jakarta.persistence.EntityManager#persist(Object)
     */
    Entity persist(Entity entity);

    default List<Entity> findAll(List<Criteria> filters, String entityGraph, Sort sort) {
        return findAll(filters, entityGraph, sort, -1);
    }

    List<Entity> findAll(List<Criteria> filters, String entityGraph, Sort sort, int limit);

    Page<Entity> findAll(List<Criteria> filters, String entityGraph, Pageable pageable);

    /**
     * Like {@link #findAll(List, String, Pageable)} but skips the count query.
     * Fetches {@code pageSize + 1} rows to determine {@link Slice#hasNext()}
     * without a separate {@code COUNT(*)}. Prefer this for large tables
     * when the caller only needs "is there a next page?" rather than total count.
     */
    Slice<Entity> findSlice(List<Criteria> filters, String entityGraph, Pageable pageable);
}
