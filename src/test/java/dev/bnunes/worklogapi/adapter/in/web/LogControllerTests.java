package dev.bnunes.worklogapi.adapter.in.web;

import dev.bnunes.worklogapi.application.port.in.RegisterLogUseCase;
import dev.bnunes.worklogapi.domain.model.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security is covered by the acceptance tests: the resource server auto-configuration is left out of the slice and the
 * principal is set by hand to test the controller alone.
 */
@WebMvcTest(controllers = LogController.class,
        excludeAutoConfiguration = OAuth2ResourceServerWebSecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class LogControllerTests {

    private static final Instant NOW = Instant.parse("2026-10-21T21:44:44Z");

    private final MockMvc mockMvc;

    @MockitoBean
    private RegisterLogUseCase registerLog;

    @Autowired
    LogControllerTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void registersTheLogForThePrincipalAndAnswers201WithoutTheOwner() throws Exception {
        given(registerLog.register("5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11", "lorem ipsum dolor sit amet"))
                .willReturn(new Log(1, "5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11", "lorem ipsum dolor sit amet",
                        true, NOW, NOW));

        mockMvc.perform(post("/worklog-api/v1/logs")
                        .principal(() -> "5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"lorem ipsum dolor sit amet\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {"id":1,"content":"lorem ipsum dolor sit amet","active":true,
                         "createdAt":"2026-10-21T21:44:44Z","updatedAt":"2026-10-21T21:44:44Z"}
                        """, JsonCompareMode.STRICT));
    }
}
