package com.egen.ecom.dto.request;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class BulkOrdersCreateRequest {
    @NotNull
	@NotEmpty
	@Valid
	List<OrderCreateRequest> orders;
}
