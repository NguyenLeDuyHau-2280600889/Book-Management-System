package fit.hutech.spring.controllers;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.entities.BookRepository;
import fit.hutech.spring.entities.Order;
import fit.hutech.spring.entities.OrderItem;
import fit.hutech.spring.entities.OrderRepository;
import fit.hutech.spring.orders.OrderCreateForm;
import fit.hutech.spring.orders.OrderEditForm;
import fit.hutech.spring.orders.OrderItemUpdateForm;
import fit.hutech.spring.user.User;
import fit.hutech.spring.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public OrderController(OrderRepository orderRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public String listOrders(Model model, Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        List<Order> orders;
        if (isAdmin) {
            orders = orderRepository.findAllByOrderByCreatedAtDesc();
        } else {
            User user = authentication == null ? null : userRepository.findByUsername(authentication.getName());
            orders = user == null ? List.of() : orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        }
        model.addAttribute("orders", orders);
        model.addAttribute("isAdmin", isAdmin);
        return "order/list";
    }

    @GetMapping("/add")
    public String addOrderForm(Model model) {
        model.addAttribute("form", new OrderCreateForm());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("books", bookRepository.findAll());
        return "order/add";
    }

    @PostMapping("/add")
    public String addOrderSubmit(@ModelAttribute("form") OrderCreateForm form) {
        if (form.getUserId() == null || form.getBookId() == null || form.getQuantity() == null) {
            return "redirect:/orders/add";
        }
        Optional<User> user = userRepository.findById(form.getUserId());
        Optional<Book> book = bookRepository.findById(form.getBookId());
        if (user.isEmpty() || book.isEmpty()) {
            return "redirect:/orders/add";
        }
        int quantity = Math.max(1, form.getQuantity());
        OrderItem orderItem = new OrderItem(null, book.get(), quantity, book.get().getPrice());
        List<OrderItem> orderItems = new java.util.ArrayList<>();
        orderItems.add(orderItem);
        Order order = new Order(null, user.get(), orderItems, orderItem.getPrice() * quantity, null, null, null, null);
        orderRepository.save(order);
        return "redirect:/orders";
    }

    @GetMapping("/edit/{id}")
    public String editOrderForm(@PathVariable Long id, Model model) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()) {
            return "redirect:/orders";
        }
        OrderEditForm form = new OrderEditForm();
        List<OrderItemUpdateForm> items = order.get().getItems().stream()
                .map(item -> new OrderItemUpdateForm(item.getId(), item.getBook().getTitle(), item.getPrice(), item.getQuantity()))
                .collect(Collectors.toList());
        form.setItems(items);
        model.addAttribute("order", order.get());
        model.addAttribute("form", form);
        return "order/edit";
    }

    @PostMapping("/edit/{id}")
    public String editOrderSubmit(@PathVariable Long id, @ModelAttribute("form") OrderEditForm form) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()) {
            return "redirect:/orders";
        }
        List<OrderItemUpdateForm> formItems = form.getItems() == null ? List.of() : form.getItems();
        Map<Long, OrderItemUpdateForm> updates = formItems.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(OrderItemUpdateForm::getId, Function.identity()));
        double total = 0;
        for (OrderItem item : order.get().getItems()) {
            OrderItemUpdateForm update = updates.get(item.getId());
            if (update != null) {
                item.setQuantity(Math.max(1, update.getQuantity()));
            }
            total += item.getPrice() * item.getQuantity();
        }
        order.get().setTotal(total);
        orderRepository.save(order.get());
        return "redirect:/orders";
    }

    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id) {
        orderRepository.deleteById(id);
        return "redirect:/orders";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam("status") String status, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return "redirect:/orders";
        }
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()) {
            return "redirect:/orders";
        }
        String normalized = normalizeStatus(status);
        if (normalized == null) {
            return "redirect:/orders";
        }
        order.get().setStatus(normalized);
        orderRepository.save(order.get());
        return "redirect:/orders";
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (normalized.equals("PENDING") || normalized.equals("APPROVED") || normalized.equals("REJECTED")) {
            return normalized;
        }
        return null;
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
