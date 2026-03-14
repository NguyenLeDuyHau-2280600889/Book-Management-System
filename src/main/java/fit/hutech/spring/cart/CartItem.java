package fit.hutech.spring.cart;

import fit.hutech.spring.entities.Book;

public class CartItem {
    private Book book;
    private int quantity;

    public CartItem(Book book, int quantity) {
        this.book = book;
        this.quantity = quantity;
    }

    public Book getBook() {
        return book;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getLineTotal() {
        if (book == null || book.getPrice() == null) {
            return 0.0;
        }
        return book.getPrice() * quantity;
    }
}
