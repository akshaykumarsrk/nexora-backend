package com.nexora.nexora_backend.converter;

import com.nexora.nexora_backend.dto.customer.CustomerResponse;
import com.nexora.nexora_backend.entity.Customer;
import com.nexora.nexora_backend.entity.Organization;
import org.springframework.stereotype.Component;
import com.nexora.nexora_backend.dto.customer.CustomerRequest;

public class CustomerConverter {

    public static Customer customerRequestToCustomer(CustomerRequest customerRequest) {

        return Customer.builder()
                .name(customerRequest.getName())
                .phone(customerRequest.getPhone())
                .email(customerRequest.getEmail())
                .customerType(customerRequest.getCustomerType())
                .gstNumber(customerRequest.getGstNumber())
                .notes(customerRequest.getNotes())
                .build();

    }

    public static CustomerResponse customerToCustomerResponse(Customer customer) {

        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .customerType(customer.getCustomerType())
                .gstNumber(customer.getGstNumber())
                .notes(customer.getNotes())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();

    }

    public static void updateCustomerFromRequest(Customer customer, CustomerRequest request) {

        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setCustomerType(request.getCustomerType());
        customer.setGstNumber(request.getGstNumber());
        customer.setNotes(request.getNotes());

    }
}
