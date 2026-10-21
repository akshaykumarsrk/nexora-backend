package com.nexora.nexora_backend.converter;

import com.nexora.nexora_backend.dto.site.SiteRequest;
import com.nexora.nexora_backend.dto.site.SiteResponse;
import com.nexora.nexora_backend.entity.Customer;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.entity.Site;

public class SiteConverter {

    public static Site siteRequestToSite(SiteRequest siteRequest, Organization organization, Customer customer) {

        return Site.builder()
                .organization(organization)
                .customer(customer)
                .name(siteRequest.getName())
                .address(siteRequest.getAddress())
                .city(siteRequest.getCity())
                .state(siteRequest.getState())
                .pincode(siteRequest.getPincode())
                .contactPerson(siteRequest.getContactPerson())
                .contactPhone(siteRequest.getContactPhone())
                .notes(siteRequest.getNotes())
                .build();

    }

    public static SiteResponse siteToSiteResponse(Site site) {

        return SiteResponse.builder()
                .id(site.getId())
                .customerId(site.getId())
                .name(site.getName())
                .address(site.getAddress())
                .city(site.getCity())
                .state(site.getState())
                .pincode(site.getPincode())
                .contactPerson(site.getContactPerson())
                .contactPhone(site.getContactPhone())
                .notes(site.getNotes())
                .createdAt(site.getCreatedAt())
                .updatedAt(site.getUpdatedAt())
                .build();

    }

    public static void updateSiteFromRequest(Site site, SiteRequest siteRequest) {

        site.setName(siteRequest.getName());
        site.setAddress(siteRequest.getAddress());
        site.setCity(siteRequest.getCity());
        site.setState(siteRequest.getState());
        site.setPincode(siteRequest.getPincode());
        site.setContactPerson(siteRequest.getContactPerson());
        site.setContactPhone(siteRequest.getContactPhone());
        site.setNotes(siteRequest.getNotes());
    }

}
