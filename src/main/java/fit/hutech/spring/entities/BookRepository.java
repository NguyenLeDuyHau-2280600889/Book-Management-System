package fit.hutech.spring.entities;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByCategoryIgnoreCase(String category);

    java.util.List<Book> findByCategoryIgnoreCase(String category);
}
