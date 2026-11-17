package io.github.ltsboot.lts.compat.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByAuthorIgnoreCaseOrderByPublishedYearAsc(String author);
}
