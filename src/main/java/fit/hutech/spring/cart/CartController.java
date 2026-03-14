package fit.hutech.spring.cart;

import fit.hutech.spring.entities.Order;
import fit.hutech.spring.entities.OrderItem;
import fit.hutech.spring.entities.OrderRepository;
import fit.hutech.spring.user.User;
import fit.hutech.spring.user.UserRepository;
import fit.hutech.spring.services.BookService;
import fit.hutech.spring.services.PaymentQrService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final BookService bookService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentQrService paymentQrService;
    private final String uploadDir;
    private final double newCustomerDiscountPercent;

    public CartController(CartService cartService, BookService bookService, UserRepository userRepository, OrderRepository orderRepository,
                          PaymentQrService paymentQrService,
                          @Value("${app.upload.dir:uploads}") String uploadDir,
                          @Value("${app.promo.new-customer-percent:15}") double newCustomerDiscountPercent) {
        this.cartService = cartService;
        this.bookService = bookService;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.paymentQrService = paymentQrService;
        this.uploadDir = uploadDir;
        this.newCustomerDiscountPercent = Math.max(0, newCustomerDiscountPercent);
    }

    @GetMapping
    public String viewCart(Model model, HttpSession session, Authentication authentication,
                           @org.springframework.web.bind.annotation.RequestParam(value = "error", required = false) String error) {
        List<CartItem> items = cartService.getCart(session);
        boolean removed = removeMissingBooks(items, session);
        if (removed && error == null) {
            error = "Mot so sach da bi xoa khoi he thong va da duoc loai bo khoi gio hang.";
        }
        model.addAttribute("items", items);
        model.addAttribute("total", cartService.getTotal(session));
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("currentUser", userRepository.findByUsername(authentication.getName()));
        }
        model.addAttribute("error", error);
        return "cart/index";
    }

    @PostMapping("/add/{id}")
    public String addToCart(@PathVariable Long id, HttpSession session) {
        bookService.getBookById(id).ifPresent(book -> cartService.addItem(session, book));
        return "redirect:/cart";
    }

    @PostMapping("/remove/{id}")
    public String removeFromCart(@PathVariable Long id, HttpSession session) {
        cartService.removeItem(session, id);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session, Authentication authentication,
                           @RequestParam(value = "error", required = false) String error) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsername(authentication.getName());
        double subtotal = cartService.getTotal(session);
        CheckoutTotalSummary totalSummary = calculateCheckoutSummary(user, subtotal);
        model.addAttribute("items", cartService.getCart(session));
        model.addAttribute("subtotal", totalSummary.subtotal());
        model.addAttribute("discountAmount", totalSummary.discountAmount());
        model.addAttribute("discountPercent", totalSummary.discountPercent());
        model.addAttribute("isNewCustomerDiscount", totalSummary.discountAmount() > 0);
        model.addAttribute("total", totalSummary.finalTotal());
        model.addAttribute("error", error);
        model.addAttribute("paymentQrImage", paymentQrService.getQrImagePath());
        model.addAttribute("paymentQrNote", paymentQrService.getQrNote());
        return "cart/checkout";
    }

    @PostMapping("/checkout")
    public String checkoutSubmit(HttpSession session, Authentication authentication,
                                 @RequestParam("shippingAddress") String shippingAddress,
                                 @RequestParam("paymentReceipt") MultipartFile paymentReceipt) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String normalizedAddress = shippingAddress == null ? "" : shippingAddress.trim();
        if (!StringUtils.hasText(normalizedAddress)) {
            return "redirect:/cart/checkout?error=Vui+long+nhap+dia+chi+giao+hang";
        }
        if (paymentReceipt == null || paymentReceipt.isEmpty()) {
            return "redirect:/cart/checkout?error=Vui+long+tai+anh+bien+lai+thanh+toan";
        }
        if (!isImageFile(paymentReceipt)) {
            return "redirect:/cart/checkout?error=Chi+chap+nhan+tep+anh+(jpg,+jpeg,+png,+webp)";
        }
        User user = userRepository.findByUsername(authentication.getName());
        List<CartItem> cartItems = cartService.getCart(session);
        if (removeMissingBooks(cartItems, session)) {
            return "redirect:/cart?error=Mot+so+sach+da+bi+xoa+nen+khong+the+thanh+toan";
        }
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }
        double subtotal = cartService.getTotal(session);
        CheckoutTotalSummary totalSummary = calculateCheckoutSummary(user, subtotal);
        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> new OrderItem(null, item.getBook(), item.getQuantity(), item.getBook().getPrice()))
                .collect(Collectors.toList());
        String paymentReceiptImage;
        try {
            paymentReceiptImage = storePaymentReceipt(paymentReceipt);
        } catch (IOException ex) {
            return "redirect:/cart/checkout?error=Khong+the+luu+anh+bien+lai,+vui+long+thu+lai";
        }
        Order order = new Order(null, user, orderItems, totalSummary.finalTotal(), normalizedAddress, paymentReceiptImage, null, null);
        orderRepository.save(order);
        cartService.clear(session);
        return "cart/checkout_success";
    }

    private CheckoutTotalSummary calculateCheckoutSummary(User user, double subtotal) {
        double normalizedSubtotal = roundMoney(subtotal);
        if (user == null || user.getId() == null || !"CUSTOMER".equalsIgnoreCase(user.getRole())) {
            return new CheckoutTotalSummary(normalizedSubtotal, 0, 0, normalizedSubtotal);
        }
        boolean hasOrderBefore = orderRepository.existsByUserId(user.getId());
        if (hasOrderBefore || newCustomerDiscountPercent <= 0) {
            return new CheckoutTotalSummary(normalizedSubtotal, 0, 0, normalizedSubtotal);
        }
        double discountAmount = roundMoney(normalizedSubtotal * (newCustomerDiscountPercent / 100.0));
        double finalTotal = roundMoney(Math.max(0, normalizedSubtotal - discountAmount));
        return new CheckoutTotalSummary(normalizedSubtotal, discountAmount, newCustomerDiscountPercent, finalTotal);
    }

    private double roundMoney(double amount) {
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private boolean removeMissingBooks(List<CartItem> items, HttpSession session) {
        boolean removed = false;
        java.util.Iterator<CartItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            if (item.getBook() == null || item.getBook().getId() == null) {
                iterator.remove();
                removed = true;
                continue;
            }
            if (bookService.getBookById(item.getBook().getId()).isEmpty()) {
                iterator.remove();
                removed = true;
            }
        }
        if (removed) {
            session.setAttribute("CART_ITEMS", items);
        }
        return removed;
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return true;
        }
        String original = file.getOriginalFilename();
        if (!StringUtils.hasText(original)) {
            return false;
        }
        String name = original.toLowerCase(Locale.ROOT);
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp");
    }

    private String storePaymentReceipt(MultipartFile file) throws IOException {
        Path receiptDir = Paths.get(uploadDir, "receipts").toAbsolutePath().normalize();
        Files.createDirectories(receiptDir);
        String ext = getFileExtension(file.getOriginalFilename());
        String fileName = "receipt-" + UUID.randomUUID() + ext;
        Path target = receiptDir.resolve(fileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/receipts/" + fileName;
    }

    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return ".png";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return ".png";
        }
        String ext = fileName.substring(dot).toLowerCase(Locale.ROOT);
        if (ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") || ext.equals(".webp")) {
            return ext;
        }
        return ".png";
    }

    private record CheckoutTotalSummary(double subtotal, double discountAmount, double discountPercent, double finalTotal) {
    }
}
