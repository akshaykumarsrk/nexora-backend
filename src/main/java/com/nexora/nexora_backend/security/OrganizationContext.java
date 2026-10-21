package com.nexora.nexora_backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class OrganizationContext {

    public Long getOrganizationId() {

        // SecurityContextHolder = Give me the currently logged-in user."
        // getAuthentication() = "Give me the authentication information."
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        // getPrincipal() = "Give me the actual logged-in user."
        // (AuthenticatedUser) = "I know this user is our custom NEXORA user."
        AuthenticatedUser user = (AuthenticatedUser)authentication.getPrincipal();

        // getOrganizationId() = "Tell me which company this user belongs to."
        return user.getOrganizationId();

    }

}

// Who is making this request?
//        ↓
// Which organization do they belong to?
//        ↓
// Organization ID


// Think of it as a small helper whose only job is:
//"Tell me which organization the currently logged-in user belongs to."
