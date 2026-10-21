package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.converter.CustomerConverter;
import com.nexora.nexora_backend.dto.customer.CustomerResponse;
import com.nexora.nexora_backend.entity.Customer;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.CustomerRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import com.nexora.nexora_backend.dto.customer.CustomerRequest;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrganizationContext organizationContext;
    private final EntityManager entityManager;

    public CustomerService(
            CustomerRepository customerRepository,
            OrganizationContext organizationContext,
            EntityManager entityManager
    ) {
        this.customerRepository = customerRepository;
        this.organizationContext = organizationContext;
        this.entityManager = entityManager;
    }

    public List<CustomerResponse> getCustomers() {

        Long organizationId =
                organizationContext.getOrganizationId();

        return customerRepository.findAllByOrganizationId(organizationId)
                .stream()
                .map(CustomerConverter::customerToCustomerResponse)
                .toList();
    }

    public CustomerResponse createCustomer(CustomerRequest request) {

        Long organizationId =
                organizationContext.getOrganizationId();

        // Why getReference()?
        // getReference() essentially gives Hibernate a reference saying:
        // "The customer belongs to Organization #1."
        // "Hibernate, give me an object/reference representing Organization #1. I don't need you to load all its data right now."
        Organization organization = entityManager.getReference(Organization.class, organizationId);

        Customer newCustomer = CustomerConverter.customerRequestToCustomer(request);

        newCustomer.setOrganization(organization);

        customerRepository.save(newCustomer);

        return CustomerConverter.customerToCustomerResponse(newCustomer);

    }

    public CustomerResponse getCustomer(Long id) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Customer customer =
                customerRepository
                        .findByIdAndOrganizationId(id, organizationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found"
                                )
                        );

        return CustomerConverter.customerToCustomerResponse(customer);
    }

    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Customer customer = customerRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        CustomerConverter.updateCustomerFromRequest(customer, request);

        customerRepository.save(customer);

        return CustomerConverter.customerToCustomerResponse(customer);
    }

    public void deleteCustomer(Long id) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Customer customer = customerRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        customerRepository.delete(customer);
    }
}