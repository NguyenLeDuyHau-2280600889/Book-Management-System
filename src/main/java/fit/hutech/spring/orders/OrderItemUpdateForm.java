package fit.hutech.spring.orders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemUpdateForm {
    private Long id;
    private String bookTitle;
    private Double price;
    private int quantity;
}
