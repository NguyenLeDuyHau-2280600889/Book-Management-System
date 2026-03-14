package fit.hutech.spring.controllers;

import fit.hutech.spring.category.Category;
import fit.hutech.spring.category.CategoryRepository;
import fit.hutech.spring.entities.BookRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    public CategoryController(CategoryRepository categoryRepository, BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public String list(Model model, @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("error", error);
        return "category/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("category", new Category());
        return "category/add";
    }

    @PostMapping("/add")
    public String addSubmit(@Valid @ModelAttribute("category") Category category, BindingResult bindingResult, Model model) {
        String name = category.getName();
        if (bindingResult.hasErrors()) {
            return "category/add";
        }
        if (name != null && categoryRepository.existsByNameIgnoreCase(name.trim())) {
            model.addAttribute("error", "Danh muc da ton tai.");
            return "category/add";
        }
        category.setName(name == null ? null : name.trim());
        categoryRepository.save(category);
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            return "redirect:/categories";
        }
        model.addAttribute("category", category);
        return "category/edit";
    }

    @PostMapping("/edit")
    public String editSubmit(@Valid @ModelAttribute("category") Category category, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "category/edit";
        }
        String name = category.getName();
        if (name != null && categoryRepository.existsByNameIgnoreCase(name.trim())) {
            Category existing = categoryRepository.findById(category.getId()).orElse(null);
            if (existing != null && !existing.getName().equalsIgnoreCase(name.trim())) {
                model.addAttribute("error", "Danh muc da ton tai.");
                return "category/edit";
            }
        }
        category.setName(name == null ? null : name.trim());
        categoryRepository.save(category);
        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            return "redirect:/categories";
        }
        String name = category.getName();
        if (name != null) {
            String normalized = name.trim();
            if (!normalized.isEmpty()) {
                java.util.List<fit.hutech.spring.entities.Book> books = bookRepository.findByCategoryIgnoreCase(normalized);
                if (!books.isEmpty()) {
                    for (fit.hutech.spring.entities.Book book : books) {
                        book.setCategory(null);
                    }
                    bookRepository.saveAll(books);
                }
            }
        }
        categoryRepository.deleteById(id);
        return "redirect:/categories";
    }
}
