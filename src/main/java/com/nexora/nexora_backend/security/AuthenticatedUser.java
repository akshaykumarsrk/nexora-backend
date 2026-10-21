package com.nexora.nexora_backend.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class AuthenticatedUser extends User {

    private final Long organizationId;

    public AuthenticatedUser(
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            Long organizationId
    ) {
        // means: Give username, password, and roles to the parent Spring User class.
        super(username, password, authorities);

        // means: Store the organization ID inside our custom user.
        this.organizationId = organizationId;
    }
}

// AuthenticatedUser — why did we create this?
//
//Your normal Spring Security User contains:
//
//username
//password
//roles
//
//But NEXORA needs one more piece of information:
//
//organizationId
//
//So we created:
//
//public class AuthenticatedUser extends User {
//
//    private final Long organizationId;