# Extended Jpa Repository Implementation Base

Whatever project I work on, I always need a project like this.
So I’ve been taking the time to implement this code in each project whenever it’s needed.
I finally decided to make it an independent project as a Spring Boot starter package.
You can use any number of parameters to filter the data. 
The Criteria.builder() is smart enough to ignore any filter whose value is null. 

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



