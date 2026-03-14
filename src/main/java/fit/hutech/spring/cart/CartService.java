package fit.hutech.spring.cart;

import fit.hutech.spring.entities.Book;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {
    private static final String CART_KEY = "CART_ITEMS";

    @SuppressWarnings("unchecked")
    public List<CartItem> getCart(HttpSession session) {
        Object existing = session.getAttribute(CART_KEY);
        if (existing instanceof List<?>) {
            return (List<CartItem>) existing;
        }
        List<CartItem> cart = new ArrayList<>();
        session.setAttribute(CART_KEY, cart);
        return cart;
    }

    public void addItem(HttpSession session, Book book) {
        if (book == null) {
            return;
        }
        List<CartItem> cart = getCart(session);
        for (CartItem item : cart) {
            if (item.getBook() != null && book.getId().equals(item.getBook().getId())) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        cart.add(new CartItem(book, 1));
    }

    public void removeItem(HttpSession session, Long bookId) {
        if (bookId == null) {
            return;
        }
        List<CartItem> cart = getCart(session);
        cart.removeIf(item -> item.getBook() != null && bookId.equals(item.getBook().getId()));
    }

    public double getTotal(HttpSession session) {
        return getCart(session).stream().mapToDouble(CartItem::getLineTotal).sum();
    }

    public void clear(HttpSession session) {
        getCart(session).clear();
    }
}
