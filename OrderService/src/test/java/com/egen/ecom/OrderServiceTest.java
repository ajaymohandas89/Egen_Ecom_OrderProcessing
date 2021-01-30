package com.egen.ecom;

import com.egen.ecom.dto.request.OrderCreateRequest;
import com.egen.ecom.dto.response.ItemServiceResponse;
import com.egen.ecom.dto.response.PaymentServiceResponse;
import com.egen.ecom.model.Order;
import com.egen.ecom.repository.AddressRepository;
import com.egen.ecom.repository.OrderRepository;
import com.egen.ecom.service.ItemService;
import com.egen.ecom.service.OrderService;
import com.egen.ecom.service.PaymentService;
import com.egen.ecom.util.OrderIdGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {
    @InjectMocks
    OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private ItemService itemService;

    @Mock
    private OrderIdGenerator squenceGenerator;

    @Test
    public void saveOrderTest(){
        Long orderId = 20L;
        OrderCreateRequest orderReq = CommonUtil.mockOrderCreateReq();
        List<PaymentServiceResponse> transactions = CommonUtil.mockPaymentServiceResponseList();
        List<ItemServiceResponse> itemServiceResponseList = CommonUtil.mockItemServiceResponseList();

        Mockito.when(orderRepository.save(Mockito.any())).thenReturn(CommonUtil.mockOrder());
        Mockito.when( paymentService.savePayment(Mockito.any(),Mockito.any())).thenReturn(CommonUtil.mockPayment());
        Mockito.when(itemService.saveItem(Mockito.any(),Mockito.any())).thenReturn(CommonUtil.mockItem());
        Mockito.when(addressRepository.findById(Mockito.any())).thenReturn(Optional.of(CommonUtil.mockAddress()));
        Order order = orderService.saveOrder(orderId,orderReq,transactions,itemServiceResponseList);

        Assert.assertNotNull(order);
    }
}
