package com.egen.ecom.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.transaction.Transactional;
import javax.xml.bind.DatatypeConverter;

import com.egen.ecom.dto.request.OrderCreateRequest;
import com.egen.ecom.dto.request.PaymentRequest;
import com.egen.ecom.dto.response.PaymentServiceResponse;
import com.egen.ecom.model.CardDetail;
import com.egen.ecom.model.Order;
import com.egen.ecom.model.Payment;
import com.egen.ecom.repository.CardDetailRepository;
import com.egen.ecom.repository.PaymentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
@Transactional
public class PaymentService {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Value("${service.payment.api.url}")
	private String paymentURI;

	@Autowired
	private CardDetailRepository cardDetailRepository;

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private RestTemplateHelper restTemplateHelper;

	public Payment savePayment(PaymentServiceResponse paymentsData, Order order) {
		LOG.info("database operation for payment details initialized");
		CardDetail cardDetail = saveCardDetail(paymentsData);
		Payment payment = new Payment();
		payment.setId(paymentsData.getId());
		payment.setAmount(paymentsData.getAmount());
		payment.setCurrency(paymentsData.getCurrency());
		payment.setPaid(paymentsData.isPaid());
		payment.setMethod(paymentsData.getPayment_method_details().getType());
		payment.setReceipt(paymentsData.getReceipt_url());
		payment.setStatus(paymentsData.getStatus());
		payment.setOrder(order);
		payment.setCardDetail(cardDetail);

		return paymentRepository.save(payment);
	}

	public CardDetail saveCardDetail(PaymentServiceResponse paymentsData) {
		CardDetail cardDetail = new CardDetail();
		cardDetail.setCountry(paymentsData.getPayment_method_details().getCard().getCountry());
		cardDetail.setExpiredMonth(paymentsData.getPayment_method_details().getCard().getExpiredMonth());
		cardDetail.setExpiredYear(paymentsData.getPayment_method_details().getCard().getExpiredYear());
		cardDetail.setFingerprint(paymentsData.getPayment_method_details().getCard().getFingerprint());
		cardDetail.setLastFourDigit(paymentsData.getPayment_method_details().getCard().getLastFourDigit());
		cardDetail.setNetwork(paymentsData.getPayment_method_details().getCard().getNetwork());
		return cardDetailRepository.save(cardDetail);
	}

	public MultiValueMap<String, String> generatePaymentsPayload(PaymentRequest payment) {
		// setting up the request body
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("amount", String.valueOf((long) (Double.parseDouble(payment.getAmount()) * 100)));
		body.add("currency", payment.getCurrency());
		body.add("source", payment.getStored_card_name());
		return body;
	}

	public MultiValueMap<String, String> getPaymentHeaders() {
		// set up the basic authentication header
		String authorizationHeader = "Basic "
				+ DatatypeConverter.printBase64Binary(("sk_test_4eC39HqLyjWDarjtT1zdp7dc" + ":" + "").getBytes());
		// setting up the request headers
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		headers.add("Authorization", authorizationHeader);
		return headers;
	}

	public List<Future<?>> intiatePayment(OrderCreateRequest orderReq) {
		LOG.info("REST Request for Payments intialized");
		List<Future<?>> paymentFuture = new ArrayList<>();
		for (PaymentRequest payment : orderReq.getPayments()) {
			Future<?> paymentResponse = restTemplateHelper.postForEntity(PaymentServiceResponse.class, List.class,
					paymentURI, getPaymentHeaders(), generatePaymentsPayload(payment));
			paymentFuture.add(paymentResponse);
		}
		LOG.info("REST Request for Payments done Successfully");
		return paymentFuture;
	}
}
