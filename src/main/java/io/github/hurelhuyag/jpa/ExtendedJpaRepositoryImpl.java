package io.github.hurelhuyag.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import jakarta.persistence.metamodel.Attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringTokenizer;

public class ExtendedJpaRepositoryImpl<E, I> extends SimpleJpaRepository<E, I> implements ExtendedJpaRepository<E, I>{

    private final EntityManager entityManager;
    private final Class<E> domainClass;

    public ExtendedJpaRepositoryImpl(JpaEntityInformation<E, ?> entityInformation, EntityManager em) {
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
    public List<E> findAll(List<Criteria> filters, String fetchGraph, Sort sort, int limit) {
        final var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(domainClass);
        final var root = cq.from(domainClass);

        cq = cq
            .select(root)
            .where(buildPredicate(cb, cq, filters, root))
            .orderBy(QueryUtils.toOrders(sort, root, cb));

        var q = entityManager.createQuery(cq);
        if (fetchGraph != null) {
            q.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph(fetchGraph));
            //q.setHint("jakarta.persistence.loadgraph", entityManager.getEntityGraph(entityGraph));
        }

        if (limit > 0) {
            q.setMaxResults(limit);
            q.setHint("org.hibernate.fetchSize", limit);
        }

        return q.getResultList();
    }

    @Override
    public Page<E> findAll(List<Criteria> filters, String fetchGraph, Pageable pageable) {
        final var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(domainClass);
        final var root = cq.from(domainClass);

        cq = cq
            .select(root)
            .where(buildPredicate(cb, cq, filters, root))
            .orderBy(QueryUtils.toOrders(pageable.getSort(), root, cb));

        var q = entityManager.createQuery(cq);
        if (fetchGraph != null) {
            q.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph(fetchGraph));
            //q.setHint("jakarta.persistence.loadgraph", entityManager.getEntityGraph(entityGraph));
        }

        if (!pageable.isPaged()) {
            // Unpaged is one page holding everything, so the count is what came back -- no count query, and no
            // -1 for a total the caller can see for itself.
            final var result = q.getResultList();
            return new PageImpl<>(result, pageable, result.size());
        }

        q.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
        q.setMaxResults(pageable.getPageSize());
        q.setHint("org.hibernate.fetchSize", pageable.getPageSize());

        final var countQuery = cb.createQuery(Long.class);
        final var cRoot = countQuery.from(domainClass);
        final var ccq = countQuery.select(cb.count(cRoot)).where(buildPredicate(cb, countQuery, filters, cRoot));
        final long total = entityManager.createQuery(ccq).getSingleResult();

        final var result = q.getResultList();
        return new PageImpl<>(result, pageable, total);
    }

    @Override
    public Slice<E> findSlice(List<Criteria> filters, String fetchGraph, Pageable pageable) {
        final var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(domainClass);
        final var root = cq.from(domainClass);

        cq = cq
            .select(root)
            .where(buildPredicate(cb, cq, filters, root))
            .orderBy(QueryUtils.toOrders(pageable.getSort(), root, cb));

        var q = entityManager.createQuery(cq);
        if (fetchGraph != null) {
            q.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph(fetchGraph));
        }

        if (!pageable.isPaged()) {
            return new SliceImpl<>(q.getResultList(), pageable, false);
        }

        final int pageSize = pageable.getPageSize();
        q.setFirstResult(pageSize * pageable.getPageNumber());
        // Fetch one extra row to detect whether a next page exists, avoiding a COUNT query.
        q.setMaxResults(pageSize + 1);
        q.setHint("org.hibernate.fetchSize", pageSize + 1);

        var content = q.getResultList();
        final boolean hasNext = content.size() > pageSize;
        if (hasNext) {
            content = content.subList(0, pageSize);
        }
        return new SliceImpl<>(content, pageable, hasNext);
    }

    /**
     * The filters as one predicate. A filter whose path stays on the root's row -- a column, or a to-one
     * association's column -- is a plain condition. A filter whose path crosses a collection ("children.id",
     * "roles.division.id") is answered with a correlated {@code EXISTS} instead of a join, because a join returns
     * the root once per matching element: an entity with two matching children came back twice, which a page
     * cannot absorb -- a duplicate is a row lost off the end of the page and a count that is wrong.
     * <p>
     * Filters that cross the same collection share one subquery, so they have to be satisfied by the same
     * element: "children.name = a AND children.type = b" asks for one child that is both, which is what a join
     * would have said too. Filters on different collections are independent subqueries.
     */
    private Predicate buildPredicate(CriteriaBuilder cb, AbstractQuery<?> query, List<Criteria> filters, Root<E> root) {
        if (filters.isEmpty()) {
            return cb.conjunction();
        }
        final var predicates = new ArrayList<Predicate>();
        final var byCollection = new LinkedHashMap<String, List<Criteria>>();
        for (var filter : filters) {
            final var collection = collectionPrefix(root, filter.attr());
            if (collection == null) {
                predicates.add(buildPredicate(cb, filter, root, filter.attr()));
            } else {
                byCollection.computeIfAbsent(collection, k -> new ArrayList<>()).add(filter);
            }
        }
        for (var entry : byCollection.entrySet()) {
            final var subquery = query.subquery(Integer.class);
            final var correlated = subquery.correlate(root);
            From<?, ?> element = correlated;
            for (var part : entry.getKey().split("\\.")) {
                element = element.join(part);
            }
            final var inner = new ArrayList<Predicate>();
            for (var filter : entry.getValue()) {
                inner.add(buildPredicate(cb, filter, element, filter.attr().substring(entry.getKey().length() + 1)));
            }
            subquery.select(cb.literal(1)).where(cb.and(inner.toArray(Predicate[]::new)));
            predicates.add(cb.exists(subquery));
        }
        return cb.and(predicates.toArray(Predicate[]::new));
    }

    /**
     * The path up to and including the first collection-valued attribute it crosses, or null when it crosses
     * none. "roles.division.id" on an entity whose {@code roles} is a Set gives "roles"; "ref.id" gives null.
     * Only the first collection matters: inside the EXISTS a further collection may join freely, duplicates
     * there change nothing.
     */
    private String collectionPrefix(From<?, ?> root, String attrPath) {
        final var parts = attrPath.split("\\.");
        var type = entityManager.getMetamodel().managedType(root.getJavaType());
        final var prefix = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            final Attribute<?, ?> attribute = type.getAttribute(parts[i]);
            prefix.append(i == 0 ? "" : ".").append(parts[i]);
            if (attribute.isCollection()) {
                return prefix.toString();
            }
            type = entityManager.getMetamodel().managedType(attribute.getJavaType());
        }
        return null;
    }

    private Predicate buildPredicate(CriteriaBuilder cb, Criteria filter, From<?, ?> from, String attrPath) {
        final var value = filter.arg();
        final var attr = resolvePath(from, attrPath);

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
        return expr;
    }

    private Path<?> resolvePath(From<?, ?> root, String attrPath) {
        final var tokenizer = new StringTokenizer(attrPath, ".");

        From<?, ?> join = root;
        var part = tokenizer.nextToken();

        while (tokenizer.hasMoreTokens()) {
            join = join.join(part);
            part = tokenizer.nextToken();
        }

        return join.get(part);
    }
}
