package com.egen.ecom.dto.response;
import java.util.UUID;

import lombok.Data;

@Data
public class AddressResponse {
    private UUID addressId;
	private String lineOne;
	private String lineTwo;
	private String city;
	private String state;
	private String zip;
	private String type;
}
