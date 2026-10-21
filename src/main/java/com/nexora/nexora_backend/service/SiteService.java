package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.converter.SiteConverter;
import com.nexora.nexora_backend.dto.site.SiteRequest;
import com.nexora.nexora_backend.dto.site.SiteResponse;
import com.nexora.nexora_backend.entity.Customer;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.entity.Site;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.CustomerRepository;
import com.nexora.nexora_backend.repository.SiteRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final CustomerRepository customerRepository;
    private final OrganizationContext organizationContext;
    private final EntityManager entityManager;

    public SiteService(
            SiteRepository siteRepository,
            CustomerRepository customerRepository,
            OrganizationContext organizationContext,
            EntityManager entityManager
    ) {
        this.siteRepository = siteRepository;
        this.customerRepository = customerRepository;
        this.organizationContext = organizationContext;
        this.entityManager = entityManager;
    }

    public List<SiteResponse> getSites() {

        Long organizationId = organizationContext.getOrganizationId();

        return siteRepository.findAllByOrganizationId(organizationId)
                .stream()
                .map(SiteConverter::siteToSiteResponse)
                .toList();

    }

    public SiteResponse getSite(Long id) {

        Long organizationId = organizationContext.getOrganizationId();

        Site site = siteRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found"));

        return SiteConverter.siteToSiteResponse(site);
    }

    public SiteResponse createSite(SiteRequest siteRequest) {

        Long organizationId = organizationContext.getOrganizationId();

        Customer customer = customerRepository.findByIdAndOrganizationId(siteRequest.getCustomerId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Organization organization = entityManager.getReference(
                Organization.class,
                organizationId
        );

        Site site = SiteConverter.siteRequestToSite(siteRequest, organization, customer);

        siteRepository.save(site);
        return SiteConverter.siteToSiteResponse(site);

    }

    public SiteResponse updateSite(Long id, SiteRequest siteRequest) {

        Long organizationId = organizationContext.getOrganizationId();

        Site site = siteRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found"));

        Customer customer = customerRepository.findByIdAndOrganizationId(siteRequest.getCustomerId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        site.setCustomer(customer);

        SiteConverter.updateSiteFromRequest(site, siteRequest);

        siteRepository.save(site);

        return SiteConverter.siteToSiteResponse(site);

    }

    public void deleteSite(Long id) {

        Long organizationId = organizationContext.getOrganizationId();

        Site site = siteRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found"));

        siteRepository.delete(site);

    }

    public List<SiteResponse> getSitesByCustomer(Long customerId) {

        Long organizationId = organizationContext.getOrganizationId();

        // First verify that the customer belongs
        // to the current organization
        customerRepository.findByIdAndOrganizationId(customerId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return siteRepository.findAllByCustomerIdAndOrganizationId(customerId, organizationId)
                .stream()
                .map(SiteConverter::siteToSiteResponse)
                .toList();

    }
}