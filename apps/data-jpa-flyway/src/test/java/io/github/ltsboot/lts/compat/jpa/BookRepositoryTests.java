package io.github.ltsboot.lts.compat.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class BookRepositoryTests {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private BookRepository books;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void flywayMigrationIsAppliedBeforeHibernateValidates() {
        Integer applied = jdbc.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1' AND success", Integer.class);

        assertThat(applied).isEqualTo(1);
    }

    @Test
    void savedBookIsGeneratedIdAndReadBack() {
        Book saved = books.saveAndFlush(new Book("SICP", "Abelson", 1985));

        assertThat(saved.getId()).isNotNull();
        assertThat(books.findById(saved.getId())).isPresent()
                .get().extracting(Book::getTitle).isEqualTo("SICP");
    }

    @Test
    void derivedQueryMatchesAuthorIgnoringCaseAndSortsByYear() {
        books.saveAndFlush(new Book("Structure and Interpretation", "Abelson", 1985));
        books.saveAndFlush(new Book("Lisp in Small Pieces", "Queinnec", 1996));
        books.saveAndFlush(new Book("Early Abelson", "ABELSON", 1979));

        List<Book> result = books.findByAuthorIgnoreCaseOrderByPublishedYearAsc("abelson");

        assertThat(result).extracting(Book::getPublishedYear).containsExactly(1979, 1985);
    }
}
