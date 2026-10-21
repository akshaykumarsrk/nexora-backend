package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.converter.ServiceRequestConverter;
import com.nexora.nexora_backend.dto.service_request.ServiceRequestRequest;
import com.nexora.nexora_backend.dto.service_request.ServiceRequestResponse;
import com.nexora.nexora_backend.entity.Asset;
import com.nexora.nexora_backend.entity.Customer;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.entity.ServiceRequest;
import com.nexora.nexora_backend.entity.Site;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.AssetRepository;
import com.nexora.nexora_backend.repository.CustomerRepository;
import com.nexora.nexora_backend.repository.ServiceRequestRepository;
import com.nexora.nexora_backend.repository.SiteRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final CustomerRepository customerRepository;
    private final SiteRepository siteRepository;
    private final AssetRepository assetRepository;
    private final OrganizationContext organizationContext;
    private final EntityManager entityManager;

    public List<ServiceRequestResponse> getServiceRequests() {

        Long organizationId =
                organizationContext.getOrganizationId();

        return serviceRequestRepository
                .findAllByOrganizationId(organizationId)
                .stream()
                .map(ServiceRequestConverter::entityToResponse)
                .toList();
    }

    public ServiceRequestResponse getServiceRequest(Long id) {

        Long organizationId =
                organizationContext.getOrganizationId();

        ServiceRequest serviceRequest =
                serviceRequestRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Service request not found"
                                )
                        );

        return ServiceRequestConverter.entityToResponse(
                serviceRequest
        );
    }

    public ServiceRequestResponse createServiceRequest(
            ServiceRequestRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Customer customer =
                customerRepository
                        .findByIdAndOrganizationId(
                                request.getCustomerId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found"
                                )
                        );

        Site site =
                siteRepository
                        .findByIdAndOrganizationId(
                                request.getSiteId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Site not found"
                                )
                        );

        // Make sure the site belongs to the customer.
        if (!site.getCustomer().getId()
                .equals(customer.getId())) {

            throw new ResourceNotFoundException(
                    "Site does not belong to customer"
            );
        }

        Asset asset = null;

        if (request.getAssetId() != null) {

            asset =
                    assetRepository
                            .findByIdAndOrganizationId(
                                    request.getAssetId(),
                                    organizationId
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Asset not found"
                                    )
                            );

            // Make sure the asset belongs to the selected site.
            if (!asset.getSite().getId()
                    .equals(site.getId())) {

                throw new ResourceNotFoundException(
                        "Asset does not belong to site"
                );
            }
        }

        Organization organization =
                entityManager.getReference(
                        Organization.class,
                        organizationId
                );

        ServiceRequest serviceRequest =
                ServiceRequestConverter.requestToEntity(
                        request,
                        customer,
                        site,
                        asset
                );

        serviceRequest.setOrganization(
                organization
        );

        ServiceRequest savedServiceRequest =
                serviceRequestRepository.save(
                        serviceRequest
                );

        return ServiceRequestConverter.entityToResponse(
                savedServiceRequest
        );
    }

    public ServiceRequestResponse updateServiceRequest(
            Long id,
            ServiceRequestRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();


        ServiceRequest serviceRequest =
                serviceRequestRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Service request not found"
                                )
                        );


        Customer customer =
                customerRepository
                        .findByIdAndOrganizationId(
                                request.getCustomerId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found"
                                )
                        );


        Site site =
                siteRepository
                        .findByIdAndOrganizationId(
                                request.getSiteId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Site not found"
                                )
                        );


        // Site must belong to customer

        if (!site.getCustomer().getId()
                .equals(customer.getId())) {

            throw new ResourceNotFoundException(
                    "Site does not belong to customer"
            );
        }


        Asset asset = null;

        if (request.getAssetId() != null) {

            asset =
                    assetRepository
                            .findByIdAndOrganizationId(
                                    request.getAssetId(),
                                    organizationId
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Asset not found"
                                    )
                            );


            // Asset must belong to site

            if (!asset.getSite().getId()
                    .equals(site.getId())) {

                throw new ResourceNotFoundException(
                        "Asset does not belong to site"
                );
            }
        }


        ServiceRequestConverter.updateEntityFromRequest(
                serviceRequest,
                request,
                customer,
                site,
                asset
        );


        ServiceRequest updatedServiceRequest =
                serviceRequestRepository.save(
                        serviceRequest
                );


        return ServiceRequestConverter.entityToResponse(
                updatedServiceRequest
        );
    }
}