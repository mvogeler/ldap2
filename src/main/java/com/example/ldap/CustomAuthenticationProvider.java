package com.example.ldap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.OperationNotSupportedException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.List;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Value("${ldap.url}")
    private String ldapUrl;

    @Value("${ldap.base.dn}")
    private String ldapBaseDn;

    @Value("${ldap.username}")
    private String ldapSecurityPrincipal;

    @Value("${ldap.password}")
    private String ldapPrincipalPassword;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String username = authentication.getName();
        String password = (String) authentication.getCredentials();
        LdapQuery query = LdapQueryBuilder.query()
                .base(ldapBaseDn)
                .where("objectclass").is("inetOrgPerson")
                .and("uid").is(username);

        LdapTemplate ldapTemplate = getLdapTemplate();

        try {
            ldapTemplate.authenticate(query, password);
        } catch (EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException("User '" + username + "' does not exist!");
        } catch (org.springframework.ldap.AuthenticationException e) {
            throw new BadCredentialsException("Invalid Username/Password Combination!");
        } catch (OperationNotSupportedException e) {
            throw new InsufficientAuthenticationException("Password Required!");
        }

        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                username, password,
                getRoles(username));

        result.setDetails(authentication.getDetails());

        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    public List<GrantedAuthority> getRoles(String username) {
        LdapQuery groupQuery = LdapQueryBuilder.query().base(ldapBaseDn)
                .where("objectclass").is("groupOfUniqueNames");

        List<Group> groupList = getLdapTemplate().search(groupQuery, new GroupAttributesMapper());

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Group group : groupList) {
            if (group.getUniqueMembers().contains("uid=" + username + "," + ldapBaseDn)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + group.getName().toUpperCase()));
            }
        }

        return authorities;
    }

    private class GroupAttributesMapper implements AttributesMapper {

        @Override
        public Object mapFromAttributes(Attributes attributes) throws NamingException {
            Group group = new Group();
            group.setName((String) attributes.get("cn").get());

            NamingEnumeration attr = attributes.get("uniqueMember").getAll();
            while (attr.hasMore()) {
                String member = (String) attr.next();
                group.addUniqueMember(member);
            }

            return group;
        }
    }

    private LdapTemplate getLdapTemplate() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrl);
        contextSource.setUserDn(ldapSecurityPrincipal);
        contextSource.setPassword(ldapPrincipalPassword);
        try {
            contextSource.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LdapTemplate ldapTemplate = new LdapTemplate();
        ldapTemplate.setContextSource(contextSource);
        return ldapTemplate;
    }
}
