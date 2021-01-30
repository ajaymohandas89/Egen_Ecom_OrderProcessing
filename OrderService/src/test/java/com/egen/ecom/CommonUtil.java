package com.egen.ecom;

import com.egen.ecom.dto.request.AddressRequest;
import com.egen.ecom.dto.request.ItemRequest;
import com.egen.ecom.dto.request.OrderCreateRequest;
import com.egen.ecom.dto.response.ItemServiceResponse;
import com.egen.ecom.dto.response.PaymentServiceResponse;
import com.egen.ecom.enums.DeliveryTypeEnum;
import com.egen.ecom.enums.OrderStatusEnum;
import com.egen.ecom.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommonUtil {

    public static ItemServiceResponse mockItemServiceResponse(){
        ItemServiceResponse itemServiceResponse = new ItemServiceResponse();
        itemServiceResponse.setItemName("ITEMA");
        itemServiceResponse.setItemCost(100.12);
        itemServiceResponse.setItemDescription("DescriptionA");
        itemServiceResponse.setId("1234567");
        itemServiceResponse.setSkuId("1234567");
        return itemServiceResponse;
    }

    public static Order mockOrder(){
        Order order = new Order();
        order.setOrderID(1L);
        order.setCustomerId(UUID.randomUUID());
        order.setOrderStatus(OrderStatusEnum.ACCEPTED);
        order.setOrderSubtotal(12.12);
        order.setDeliveryType(DeliveryTypeEnum.CASH_ON_DELIVERY);
        return order;
    }

    public static Item mockItem(){
        Item item = new Item();
        item.setItemName("ITEMA");
        item.setItemCost(12.31);
        item.setItemDescription("description");
        return item;
    }

    public static OrderCreateRequest mockOrderCreateReq(){
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest();
        orderCreateRequest.setDeliveryType("ONLINE");
        orderCreateRequest.setOrderCustomerId(UUID.randomUUID());
        orderCreateRequest.setItems(mockItemRequestList());
        orderCreateRequest.setTax_fee(6.13);
        orderCreateRequest.setOrder_billing_address(mockAddressReq());
        orderCreateRequest.setOrder_shipping_address(mockAddressReq());
        return orderCreateRequest;
    }

    public static ItemRequest mockItemReq(){
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setItemName("ITEMA");
        itemRequest.setItemQuantity(12);
        return itemRequest;
    }
    public static List<ItemRequest> mockItemRequestList(){
        List<ItemRequest> itemRequestsList = new ArrayList<>();
        itemRequestsList.add(mockItemReq());
        return itemRequestsList;
    }

    public static PaymentServiceResponse mockPaymentServiceResponse(){
        PaymentServiceResponse paymentServiceResponse = new PaymentServiceResponse();
        paymentServiceResponse.setAmount(12L);
        paymentServiceResponse.setId("1234567");
        paymentServiceResponse.setPaid(true);
        paymentServiceResponse.setCurrency("Dollor");
        paymentServiceResponse.setStatus("PAID");
        return paymentServiceResponse;
    }
    public static List<PaymentServiceResponse> mockPaymentServiceResponseList(){
        List<PaymentServiceResponse> paymentServiceResponseList = new ArrayList<>();
        paymentServiceResponseList.add(mockPaymentServiceResponse());
        return paymentServiceResponseList;
    }

    public static Payment mockPayment(){
        Payment payment = new Payment();
        payment.setAmount(10L);
        payment.setCurrency("USD");
        payment.setId("123456");
        payment.setStatus("PAID");
        return payment;
    }

    public static List<ItemServiceResponse> mockItemServiceResponseList(){
        List<ItemServiceResponse> itemServiceResponses = new ArrayList<>();
        itemServiceResponses.add(mockItemServiceResponse());
        return itemServiceResponses;
    }

    public static AddressRequest mockAddressReq(){
        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setId(UUID.randomUUID());
        addressRequest.setCity("CITY");
        addressRequest.setLineOne("LIne1");
        addressRequest.setState("STATE");
        return addressRequest;
    }
    public static Address mockAddress(){
        Address address = new Address();
        address.setAddressId(UUID.randomUUID());
        address.setCity("city");
        address.setLineOne("line1");
        address.setLineTwo("line2");
        address.setState("State");
        return address;
    }
}
