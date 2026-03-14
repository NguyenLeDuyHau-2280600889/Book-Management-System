package fit.hutech.spring.services;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.entities.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Page<Book> getBooksPage(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return bookRepository.findAll();
        }
        String needle = normalizeText(keyword);
        return bookRepository.findAll().stream()
                .filter(book ->
                        containsIgnoreCase(book.getTitle(), needle)
                                || containsIgnoreCase(book.getAuthor(), needle)
                                || containsIgnoreCase(book.getCategory(), needle)
                                || String.valueOf(book.getId()).contains(needle)
                                || String.valueOf(book.getPrice()).contains(needle)
                )
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String value, String needle) {
        return value != null && normalizeText(value).contains(needle);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim().toLowerCase();
        String normalized = java.text.Normalizer.normalize(trimmed, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public void addBook(Book book) {
        bookRepository.save(book);
    }

    public void updateBook(Book book) {
        bookRepository.save(book);
    }

    public void deleteBookById(Long id) {
        bookRepository.deleteById(id);
    }
}
