package com.egen.ecom.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationTestController {

	@GetMapping("/apiTestCheck")
	public String testCheck() {
		return "Ecomerce application is up and running";
	}
}