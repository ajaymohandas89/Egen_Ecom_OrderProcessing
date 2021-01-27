package com.egen.ecom.dto.response;
import lombok.Data;

@Data
public class CardResponse {
	private String country;
	private int expiredMonth;
	private int expiredYear;
	private String fingerprint;;
	private String lastFourDigit;
	private String network;
}
