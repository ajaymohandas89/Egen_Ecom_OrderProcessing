package com.egen.ecom.dto.response;
import java.util.Date;

import lombok.Data;

@Data
public class PaymentResponse {
    private String id;
	private long amount;
	private String currency;
	private boolean paid;
	private String method;
	private CardResponse card;
	private String receipt;
	private String status;
	private Date paymentDate;
}
