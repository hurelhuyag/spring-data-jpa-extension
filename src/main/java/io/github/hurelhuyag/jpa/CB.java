package io.github.hurelhuyag.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CB {

    private final List<Criteria> criteria = new ArrayList<>();

    public CB eq(String attr, Object value) {
        if (value != null) {
            criteria.add(new Criteria(attr, Expr.EQ, value));
        }
        return this;
    }

    public CB notEq(String attr, Object value) {
        if (value != null) {
            criteria.add(new Criteria(attr, Expr.NOT_EQ, value));
        }
        return this;
    }

    public CB like(String attr, String value) {
        if (value != null) {
            criteria.add(new Criteria(attr, Expr.LIKE, value));
        }
        return this;
    }

    public CB lt(String attr, Object value) {
        if (value != null) {
            criteria.add(new Criteria(attr, Expr.LT, value));
        }
        return this;
    }

    public CB lte(String attr, Object value) {
        if (value != null) {
            criteria.add(new Criteria(attr, Expr.LTE, value));
        }
        return this;
    }

    public CB gt(String attr, Object value) {
        if (value != null) {
            criteria.add(new Criteria(attr, Expr.GT, value));
        }
        return this;
    }

    public CB gte(String attr, Object value) {
        if (value != null) {
            criteria.add(new Criteria(attr, Expr.GTE, value));
        }
        return this;
    }

    public CB in(String attr, Collection<Object> value) {
        if (value != null) {
            criteria.add(new Criteria(attr, Expr.IN, value));
        }
        return this;
    }

    public CB isNull(String attr) {
        criteria.add(new Criteria(attr, Expr.IS_NULL, null));
        return this;
    }

    public CB isNotNull(String attr) {
        criteria.add(new Criteria(attr, Expr.IS_NOT_NULL, null));
        return this;
    }

    public List<Criteria> build() {
        return criteria;
    }

}
