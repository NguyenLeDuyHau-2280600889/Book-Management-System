package fit.hutech.spring.entities;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BookInitializer implements CommandLineRunner {
    private static final int MIN_BOOKS = 8;
    private final BookRepository bookRepository;

    @Override
    public void run(String... args) {
        long currentCount = bookRepository.count();
        if (currentCount >= MIN_BOOKS) {
            return;
        }

        List<Book> defaultBooks = List.of(
                new Book(null, "Clean Code", "Robert C. Martin", 180000.0, "Cong nghe thong tin",
                        "https://picsum.photos/seed/clean-code/300/400"),
                new Book(null, "Design Patterns", "Erich Gamma", 220000.0, "Cong nghe thong tin",
                        "https://picsum.photos/seed/design-patterns/300/400"),
                new Book(null, "Refactoring", "Martin Fowler", 200000.0, "Cong nghe thong tin",
                        "https://picsum.photos/seed/refactoring/300/400"),
                new Book(null, "Effective Java", "Joshua Bloch", 190000.0, "Cong nghe thong tin",
                        "https://picsum.photos/seed/effective-java/300/400"),
                new Book(null, "Computer Networks", "Andrew S. Tanenbaum", 210000.0, "Cong nghe thong tin",
                        "https://picsum.photos/seed/computer-networks/300/400"),
                new Book(null, "Operating System Concepts", "Abraham Silberschatz", 230000.0, "Cong nghe thong tin",
                        "https://picsum.photos/seed/operating-systems/300/400"),
                new Book(null, "Database System Concepts", "Silberschatz", 205000.0, "Cong nghe thong tin",
                        "https://picsum.photos/seed/database-systems/300/400"),
                new Book(null, "Introduction to Algorithms", "Cormen", 250000.0, "Cong nghe thong tin",
                        "https://picsum.photos/seed/algorithms/300/400")
        );

        Set<String> existingTitles = bookRepository.findAll().stream()
                .map(Book::getTitle)
                .filter(title -> title != null && !title.isBlank())
                .map(title -> title.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        int missingSlots = (int) Math.max(0, MIN_BOOKS - currentCount);
        List<Book> booksToInsert = defaultBooks.stream()
                .filter(book -> !existingTitles.contains(book.getTitle().trim().toLowerCase(Locale.ROOT)))
                .limit(missingSlots)
                .toList();

        if (!booksToInsert.isEmpty()) {
            bookRepository.saveAll(booksToInsert);
        }
    }
}
