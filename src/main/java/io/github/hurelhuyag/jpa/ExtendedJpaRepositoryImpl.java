package io.github.hurelhuyag.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.util.Collection;
import java.util.List;

public class ExtendedJpaRepositoryImpl<E, I> extends SimpleJpaRepository<E, I> implements ExtendedJpaRepository<E, I>{

    private final EntityManager entityManager;
    private final Class<E> domainClass;

    public ExtendedJpaRepositoryImpl(JpaEntityInformation<E, I> entityInformation, EntityManager em) {
        super(entityInformation, em);
        this.entityManager = em;
        this.domainClass = entityInformation.getJavaType();
    }

    public ExtendedJpaRepositoryImpl(Class<E> domainClass, EntityManager em) {
        super(domainClass, em);
        this.entityManager = em;
        this.domainClass = domainClass;
    }

    /*@Override
    public <S extends E> S save(S entity) {
        throw new RuntimeException("use persist() instead");
    }*/

    @Override
    public E persist(E e) {
        entityManager.persist(e);
        return e;
    }

    @Override
    public Page<E> findAll(List<Criteria> filters, String fetchGraph, Pageable pageable) {
        final var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(domainClass);
        final var root = cq.from(domainClass);

        cq = cq
            .select(root)
            .where(buildPredicate(cb, filters, root))
            .orderBy(
                pageable.getSort()
                    .stream()
                    .map(
                        o -> o.getDirection().isAscending()
                        ? cb.asc(root.get(o.getProperty()))
                        : cb.desc(root.get(o.getProperty()))
                    )
                    .toArray(Order[]::new)
            );

        var q = entityManager.createQuery(cq);
        if (fetchGraph != null) {
            q.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph(fetchGraph));
            //q.setHint("jakarta.persistence.loadgraph", entityManager.getEntityGraph(entityGraph));
        }
        if (pageable.isPaged()) {
            q.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
            q.setMaxResults(pageable.getPageSize());
        }
        final var result = q.getResultList();

        long total = -1;
        if (pageable.isPaged()) {
            final var countQuery = cb.createQuery(Long.class);
            final var cRoot = countQuery.from(domainClass);
            final var ccq = countQuery.select(cb.count(cRoot)).where(buildPredicate(cb, filters, cRoot));
            total = entityManager.createQuery(ccq).getSingleResult();
        }

        return new PageImpl<>(result, pageable, total);
    }

    private Predicate buildPredicate(CriteriaBuilder cb, List<Criteria> filters, Root<E> root) {
        var where = cb.conjunction();
        for (var filter : filters) {
            final var attrPath = filter.attr();
            final var value = filter.arg();
            final var i = attrPath.indexOf('.');
            Path<?> attr;
            if (i > 0) {
                final var attrPath0 = attrPath.substring(0, i);
                final var attrPath1 = attrPath.substring(i+1);
                final var join = root.join(attrPath0);
                attr = join.get(attrPath1);
            } else {
                attr = root.get(attrPath);
            }

            @SuppressWarnings({"unchecked", "rawtypes"})
            var expr = switch (filter.expr()) {
                case EQ -> cb.equal(attr, value);
                case NOT_EQ -> cb.notEqual(attr, value);
                case LIKE -> cb.like((Path<String>) attr, value.toString());
                case GT -> cb.greaterThan((Expression<? extends Comparable>) attr, (Comparable) value);
                case GTE -> cb.greaterThanOrEqualTo((Expression<? extends Comparable>) attr, (Comparable) value);
                case LT -> cb.lessThan((Expression<? extends Comparable>) attr, (Comparable) value);
                case LTE -> cb.lessThanOrEqualTo((Expression<? extends Comparable>) attr, (Comparable) value);
                case IS_NULL -> attr.isNull();
                case IS_NOT_NULL -> attr.isNotNull();
                case IN -> attr.in((Collection<?>) value);
            };
            where = cb.and(where, expr);
        }
        return where;
    }
}
