package org.example.bookingsystem;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.UUID;

public class CustomUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final UUID publicId;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String username, String password, UUID publicId,
                             Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.publicId = publicId;
        this.authorities = authorities;
    }

    public UUID getPublicId() {
        return publicId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
