package io.github.hurelhuyag.jpa;

/**
 * Filter expression. I hope names understandable
 */
enum Expr {
    EQ,
    NOT_EQ,
    LIKE,
    GT,
    GTE,
    LT,
    LTE,
    IS_NULL,
    IS_NOT_NULL,
    IN;
}