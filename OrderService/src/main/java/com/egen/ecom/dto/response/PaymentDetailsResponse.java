package com.egen.ecom.dto.response;

import lombok.Data;

@Data
public class PaymentDetailsResponse {
    private CardResponse card;
	private String type;
}
