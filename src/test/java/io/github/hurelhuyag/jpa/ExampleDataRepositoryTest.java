package io.github.hurelhuyag.jpa;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Sql(statements = {
"""
INSERT INTO exampleRef(id,name) VALUES(1, 'ref1'),(2, 'ref2'),(3,'ref3'),(4,'ref4');
""",
"""
INSERT INTO exampleData(id,name,type,createdAt, ref_id) 
VALUES
    (1,'name1', 0, '2020-01-01T01:01:01', 2),
    (2,'name2', 1, '2020-01-01T01:02:01', 3),
    (3,'name3', 1, '2020-01-01T01:03:01', 4),
    (4,'name3', 0, '2020-01-01T01:04:01', 1),
    (5,'name3', 1, '2020-01-01T01:05:01', 2);
"""
})
@DataJpaTest
public class ExampleDataRepositoryTest {

    @Autowired
    ExampleDataRepository exampleDataRepository;

    @Test
    void test0() {
        var r = exampleDataRepository.findAll();
        assertThat(r).hasSize(5);
    }

    @Test
    void testEqStr() {
        var r = exampleDataRepository.findAll(List.of(new Criteria("name", Expr.EQ, "name1")), null, Pageable.unpaged());
        assertThat(r.getContent()).hasSize(1);
    }

    @Test
    void testEqInt() {
        var r = exampleDataRepository.findAll(List.of(new Criteria("id", Expr.EQ, 1)), null, Pageable.unpaged());
        assertThat(r.getContent()).hasSize(1);
    }

    @Test
    void testEqEnum() {
        var r = exampleDataRepository.findAll(List.of(new Criteria("type", Expr.EQ, ExampleData.Type.A)), null, Pageable.unpaged());
        assertThat(r.getContent()).hasSize(2);
    }

    @Test
    void testInStr() {
        var r = exampleDataRepository.findAll(List.of(new Criteria("name", Expr.IN, List.of("name1", "name2"))), null, Pageable.unpaged());
        assertThat(r.getContent()).hasSize(2);
    }

    @Test
    void testLTE() {
        var r = exampleDataRepository.findAll(List.of(new Criteria("createdAt", Expr.LTE, LocalDateTime.of(2020, 1, 1, 1, 2, 1))), null, Pageable.unpaged());
        assertThat(r.getContent()).hasSize(2);
    }

    @Test
    void testLT() {
        var r = exampleDataRepository.findAll(List.of(new Criteria("createdAt", Expr.LT, LocalDateTime.of(2020, 1, 1, 1, 2, 1))), null, Pageable.unpaged());
        assertThat(r.getContent()).hasSize(1);
    }

    @Test
    void testJoin() {
        var r = exampleDataRepository.findAll(List.of(new Criteria("ref.id", Expr.EQ, 3)), null, Pageable.unpaged());
        assertThat(r.getContent()).hasSize(1);
    }

    @Test
    void testWithCountQuery() {
        var r = exampleDataRepository.findAll(List.of(new Criteria("name", Expr.EQ, "name3")), null, PageRequest.of(0, 2));
        assertThat(r.getContent()).hasSize(2);
        assertThat(r.getTotalElements()).isEqualTo(3);
    }

    @Test
    void testWithCountQueryNextPage() {
        var r = exampleDataRepository.findAll(List.of(new Criteria("name", Expr.EQ, "name3")), null, PageRequest.of(1, 2));
        assertThat(r.getContent()).hasSize(1);
        assertThat(r.getTotalElements()).isEqualTo(3);
    }
}
