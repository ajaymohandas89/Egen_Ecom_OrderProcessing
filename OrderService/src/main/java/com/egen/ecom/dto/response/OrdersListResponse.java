package com.egen.ecom.dto.response;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class OrdersListResponse {
    private List<OrderResponse> orders = new ArrayList<>();
}
