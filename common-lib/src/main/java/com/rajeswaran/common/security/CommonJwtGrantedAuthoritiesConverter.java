package com.rajeswaran.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommonJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> defaultAuthorities = defaultConverter.convert(jwt);

        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        Collection<String> realmRoles = realmAccess != null ?
                (Collection<String>) realmAccess.get("roles") :
                List.of();

        Collection<GrantedAuthority> realmAuthorities = realmRoles.stream()
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return Stream.concat(
                defaultAuthorities != null ? defaultAuthorities.stream() : Stream.empty(),
                realmAuthorities.stream()
        ).collect(Collectors.toList());
    }
}

