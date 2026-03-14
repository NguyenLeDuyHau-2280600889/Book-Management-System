package fit.hutech.spring.controllers;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.services.BookService;
import fit.hutech.spring.entities.OrderItemRepository;
import fit.hutech.spring.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.WebDataBinder;
import jakarta.validation.Valid;

import java.beans.PropertyEditorSupport;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    private static final int BOOKS_PER_PAGE = 8;
    private final BookService bookService;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;

    @GetMapping
    public String showAllBooks(
            @NonNull Model model,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String error
    ) {
        int pageIndex = pageNo == null || pageNo < 0 ? 0 : pageNo;
        String sortField = resolveSortField(sortBy);
        Page<Book> page = bookService.getBooksPage(
                PageRequest.of(pageIndex, BOOKS_PER_PAGE, Sort.by(sortField).ascending())
        );
        model.addAttribute("books", page.getContent());
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("sortBy", sortField);
        model.addAttribute("error", error);
        return "book/list";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Double.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.trim().isEmpty()) {
                    setValue(null);
                    return;
                }
                String normalized = text.trim().replace(",", "");
                setValue(Double.valueOf(normalized));
            }
        });
    }

    @GetMapping("/search")
    public String searchBooks(@NonNull Model model, @RequestParam(required = false) String keyword) {
        model.addAttribute("books", bookService.searchBooks(keyword));
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 0);
        model.addAttribute("keyword", keyword);
        return "book/list";
    }

    @GetMapping("/add")
    public String addBookForm(@NonNull Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryRepository.findAll());
        return "book/add";
    }

    @PostMapping("/add")
    public String addBook(@Valid @ModelAttribute("book") Book book, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "book/add";
        }
        book.setId(null);
        bookService.addBook(book);
        return "redirect:/books";
    }

    @GetMapping("/edit/{id}")
    public String editBookForm(@NonNull Model model, @PathVariable long id) {
        var book = bookService.getBookById(id).orElse(null);
        model.addAttribute("book", book != null ? book : new Book());
        model.addAttribute("categories", categoryRepository.findAll());
        return "book/edit";
    }

    @PostMapping("/edit")
    public String editBook(@Valid @ModelAttribute("book") Book book, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "book/edit";
        }
        bookService.updateBook(book);
        return "redirect:/books";
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable long id) {
        if (bookService.getBookById(id).isPresent()) {
            if (orderItemRepository.existsByBookId(id)) {
                return "redirect:/books?error=Khong+the+xoa+sach+da+co+don+hang";
            }
            bookService.deleteBookById(id);
        }
        return "redirect:/books";
    }

    private String resolveSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "id";
        }
        String trimmed = sortBy.trim();
        if (trimmed.equals("id")
                || trimmed.equals("title")
                || trimmed.equals("author")
                || trimmed.equals("price")
                || trimmed.equals("category")) {
            return trimmed;
        }
        return "id";
    }
}
