package fit.hutech.spring.orders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEditForm {
    private List<OrderItemUpdateForm> items = new ArrayList<>();
}
