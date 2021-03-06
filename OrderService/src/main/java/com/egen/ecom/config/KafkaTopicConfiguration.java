package com.egen.ecom.config;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class KafkaTopicConfiguration {
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    @Value(value = "${kafka.Address}")
	private String bootstrapAddress;

	@Value(value = "${kafka.ecom.create.topic}")
	private String orderCreateTopic;

	@Value(value = "${kafka.ecom.cancel.topic}")
	private String orderCancelTopic;

	@Bean
	public KafkaAdmin kafkaAdmin() {
		Map<String, Object> configs = new HashMap<>();
		configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		return new KafkaAdmin(configs);
	}

	@Bean
	public NewTopic topic1() {
        LOG.info("Created an instance for order creation");
		return new NewTopic(orderCreateTopic, 1, (short) 1);
	}

	@Bean
	public NewTopic topic2() {
        LOG.info("Created an instance for order cacellation");
		return new NewTopic(orderCancelTopic, 1, (short) 1);
	}
}
