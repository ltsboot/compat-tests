package io.github.ltsboot.lts.compat.security;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebController {

    @GetMapping("/web/hello")
    public String hello(Authentication authentication) {
        return "Hello, " + authentication.getName();
    }

    @GetMapping("/public/status")
    public String status() {
        return "ok";
    }
}
