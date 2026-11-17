package io.github.ltsboot.lts.compat.security;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    @GetMapping("/api/hello")
    public Map<String, Object> hello(@AuthenticationPrincipal Jwt jwt) {
        return Collections.singletonMap("subject", jwt.getSubject());
    }
}
