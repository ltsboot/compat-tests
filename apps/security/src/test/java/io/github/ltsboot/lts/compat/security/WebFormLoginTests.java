package io.github.ltsboot.lts.compat.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WebFormLoginTests {

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser(username = "carol")
    void authenticatedUserCanReachWebPath() throws Exception {
        mvc.perform(get("/web/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, carol"));
    }

    @Test
    void anonymousRequestIsRedirectedToLoginPage() throws Exception {
        mvc.perform(get("/web/hello"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void formLoginWithValidCredentialsAuthenticates() throws Exception {
        mvc.perform(formLogin("/login").user("alice").password("secret"))
                .andExpect(authenticated().withUsername("alice"));
    }

    @Test
    void formLoginWithWrongPasswordFails() throws Exception {
        mvc.perform(formLogin("/login").user("alice").password("wrong"))
                .andExpect(unauthenticated());
    }

    @Test
    void publicPathIsOpenWithoutLogin() throws Exception {
        mvc.perform(get("/public/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }
}
