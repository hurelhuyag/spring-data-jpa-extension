package io.github.hurelhuyag.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "exampleData")
@NamedEntityGraph(
    name = "ExampleData.withAll",
    attributeNodes = {
        @NamedAttributeNode("ref")
    }
)
@NamedEntityGraph(
    name = "ExampleData.withChildren",
    attributeNodes = {
        @NamedAttributeNode("children")
    }
)
public class ExampleData {

    public enum Type {
        A, B
    }

    @Id
    private Integer id;

    private String name;

    @Enumerated(EnumType.ORDINAL)
    private Type type;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private ExampleRef ref;

    @OneToMany(mappedBy = "parent")
    private List<ExampleChild> children;
}
