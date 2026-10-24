package dev.bnunes.worklogapi.adapter.in.web;

import dev.bnunes.worklogapi.application.port.in.AuthenticateAccountUseCase;
import dev.bnunes.worklogapi.application.port.in.AuthenticateAccountUseCase.AuthenticationResult;
import dev.bnunes.worklogapi.domain.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticateAccountUseCase authenticateAccount;

    @Test
    void passesEmailAndPasswordToTheUseCaseAndAnswers200WithItsResult() throws Exception {
        given(authenticateAccount.authenticate("frank@frank.com", "password123"))
                .willReturn(new AuthenticationResult("a.b.c", 1800, "refresh-1"));

        mockMvc.perform(post("/worklog-api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"frank@frank.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("a.b.c"))
                .andExpect(jsonPath("$.expiresIn").value(1800))
                .andExpect(jsonPath("$.refreshToken").value("refresh-1"));
    }

    @Test
    void answers401WhenTheUseCaseRejectsTheCredentials() throws Exception {
        given(authenticateAccount.authenticate("nobody@nobody.com", "password123"))
                .willThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/worklog-api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nobody@nobody.com","password":"password123"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
