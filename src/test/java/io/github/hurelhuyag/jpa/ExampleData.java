package io.github.hurelhuyag.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "exampleData")
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

    @ManyToOne
    private ExampleRef ref;
}
