package com.nexora.nexora_backend.converter;

import com.nexora.nexora_backend.dto.service_request.ServiceRequestRequest;
import com.nexora.nexora_backend.dto.service_request.ServiceRequestResponse;
import com.nexora.nexora_backend.entity.Asset;
import com.nexora.nexora_backend.entity.Customer;
import com.nexora.nexora_backend.entity.ServiceRequest;
import com.nexora.nexora_backend.entity.Site;

public class ServiceRequestConverter {

    public static ServiceRequest requestToEntity(
            ServiceRequestRequest request,
            Customer customer,
            Site site,
            Asset asset
    ) {

        return ServiceRequest.builder()
                .customer(customer)
                .site(site)
                .asset(asset)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status("OPEN")
                .build();
    }

    public static ServiceRequestResponse entityToResponse(
            ServiceRequest serviceRequest
    ) {

        return ServiceRequestResponse.builder()
                .id(serviceRequest.getId())
                .customerId(
                        serviceRequest.getCustomer().getId()
                )
                .siteId(
                        serviceRequest.getSite().getId()
                )
                .assetId(
                        serviceRequest.getAsset() != null
                                ? serviceRequest.getAsset().getId()
                                : null
                )
                .title(serviceRequest.getTitle())
                .description(serviceRequest.getDescription())
                .priority(serviceRequest.getPriority())
                .status(serviceRequest.getStatus())
                .requestedAt(serviceRequest.getRequestedAt())
                .createdAt(serviceRequest.getCreatedAt())
                .updatedAt(serviceRequest.getUpdatedAt())
                .build();
    }

    public static void updateEntityFromRequest(
            ServiceRequest serviceRequest,
            ServiceRequestRequest request,
            Customer customer,
            Site site,
            Asset asset
    ) {

        serviceRequest.setCustomer(customer);
        serviceRequest.setSite(site);
        serviceRequest.setAsset(asset);
        serviceRequest.setTitle(request.getTitle());
        serviceRequest.setDescription(request.getDescription());
        serviceRequest.setPriority(request.getPriority());
    }
}