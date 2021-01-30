package com.egen.ecom.dto.response;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class OrderResponse {
    private Long orderID;

	private UUID orderCustomerId;

	private List<ItemServiceResponse> items;

	private Date orderedAt;

	private Date updatedAt;

	private String orderStatus;

	private Double orderSubtotal;

	private Double tax_fee;

	private Double total;

	private String deliveryType;

	private AddressResponse order_shipping_address;

	private AddressResponse order_billing_address;

	private List<PaymentResponse> payments;
}
