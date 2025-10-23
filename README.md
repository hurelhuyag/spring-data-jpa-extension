# Extended Jpa Repository Implementation Base

Whatever project I work on, I always needs project like this. 
So I take my time and implement this code each project when it needs. 
I decided to make it independent project as a spring-boot-starter package.

## Usage

Just import dependency. Spring Boot will auto import configuration.

```xml
<dependency>
    <groupId>io.github.hurelhuyag</groupId>
    <artifactId>spring-data-jpa-extension</artifactId>
    <version>1.0.1</version>
</dependency>
```

```java
import io.github.hurelhuyag.jpa.Criteria;
import io.github.hurelhuyag.jpa.ExtendedJpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

interface PersonRepository extends ExtendedJpaRepository<Person, Long> {
}

@Service
@Transactional
@RequiredArgsConstructor
class PersonService {

    private final PersonRepository personRepository;

    // unlike CrudRepository.save(Entity) method, it will directly call EntityManager.persist(Entity)
    void create() {
        personRepository.persist(new Person());
    }

    // C.builder()'s methods will ignore predicate if value is null. So You don't need to check value is null or not
    Page<MyData> findAll(String name, int age, Pageable pageable) {
        return personRepository.findAll(
            Criteria.builder()
                .like("name", name)
                .gte("age", age)
                .build(),
            "Person.withAll", // declare all relation as lazy and use fetch graph to enable join fetch for required attributes
            pageable
        );
    }
}
```



