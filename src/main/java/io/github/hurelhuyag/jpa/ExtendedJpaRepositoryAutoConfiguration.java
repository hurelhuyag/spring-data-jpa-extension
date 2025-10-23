package io.github.hurelhuyag.jpa;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EnableJpaRepositories(basePackageClasses = ExtendedJpaRepositoryImpl.class)
@ConditionalOnClass(JpaRepository.class)
public class ExtendedJpaRepositoryAutoConfiguration {
}
