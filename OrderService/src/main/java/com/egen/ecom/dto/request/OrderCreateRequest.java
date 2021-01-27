package com.egen.ecom.dto.request;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class OrderCreateRequest {
    @NotNull
	private UUID orderCustomerId;

	@Valid
	@NotEmpty
	private List<ItemRequest> items;

	@Valid
	@NotEmpty
	private List<PaymentRequest> payments;

	@NotNull
	@NotEmpty
	private String deliveryType;

	@NotNull
	private Double tax_fee;

	@NotNull
	@Valid
	private AddressRequest order_shipping_address;

	@NotNull
	@Valid
	private AddressRequest order_billing_address; 
}
