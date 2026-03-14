package fit.hutech.spring.orders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateForm {
    private Long userId;
    private Long bookId;
    private Integer quantity = 1;
}
