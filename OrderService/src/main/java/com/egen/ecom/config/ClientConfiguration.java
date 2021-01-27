package com.egen.ecom.config;

import com.egen.ecom.util.OrderIdGenerator;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfiguration {

	@Value("${spring.nodeID:0}")
	private int nodeID;

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
    }
    
    //if nodeID value is 0 then construct the nodeId and nodeId is passed then just set the value
	@Bean
	public OrderIdGenerator sequenceGenerator() {
		if (nodeID == 0) {
			return new OrderIdGenerator();
		}
		return new OrderIdGenerator(nodeID);
	}

}
