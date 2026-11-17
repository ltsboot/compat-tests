package io.github.ltsboot.lts.compat.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@SpringBootTest
@AutoConfigureMockMvc
class ApiJwtResourceServerTests {

    @Autowired
    private MockMvc mvc;

    @Value("${app.jwt.secret}")
    private String secret;

    @Test
    void jwtPostProcessorAuthenticatesApiRequest() throws Exception {
        mvc.perform(get("/api/hello").with(jwt().jwt(token -> token.subject("bob"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("bob"));
    }

    @Test
    void validSignedTokenIsAccepted() throws Exception {
        String token = sign(secret, "dave");

        mvc.perform(get("/api/hello").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("dave"));
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/hello"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() throws Exception {
        String forged = sign("some-other-signing-key-that-is-long-enough-0001", "mallory");

        mvc.perform(get("/api/hello").header(HttpHeaders.AUTHORIZATION, "Bearer " + forged))
                .andExpect(status().isUnauthorized());
    }

    private static String sign(String key, String subject) throws JOSEException {
        JWSSigner signer = new MACSigner(key.getBytes(StandardCharsets.UTF_8));
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .expirationTime(new Date(System.currentTimeMillis() + 60_000L))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(signer);
        return jwt.serialize();
    }
}
