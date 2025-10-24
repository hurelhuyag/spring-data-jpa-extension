package io.github.hurelhuyag.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "exampleChild")
public class ExampleChild {

    @Id
    private Integer id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private ExampleData parent;
}
