package com.example.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    private static Logger log = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("/")
    public String index() {

        log.info("Getting UsernamePasswordAuthenticationToken from SecurityContextHolder");
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken)
                        SecurityContextHolder.getContext().getAuthentication();

        log.info("authentication: " + authentication);
        log.info("principal: " + authentication.getPrincipal());

        return "Welcome to the home page!";
    }

    @GetMapping("/chemists")
    public String chemists(){
        return "Hello chemists";
    }

    @GetMapping("/mathematicians")
    public String mathematicians(){
        return "Hello mathematicians";
    }
}
