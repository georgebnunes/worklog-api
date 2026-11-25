package dev.bnunes.worklogapi.adapter.in.web;

import dev.bnunes.worklogapi.application.port.in.LogUseCase;
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
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    private LogUseCase logUseCase;

    @Autowired
    LogControllerTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void registersTheLogForThePrincipalAndAnswers201WithoutTheOwner() throws Exception {
        given(logUseCase.register("5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11", "lorem ipsum dolor sit amet"))
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

    @Test
    void listsTheLogsOfThePrincipalUnderDataItemsWithoutTheOwner() throws Exception {
        given(logUseCase.listActive("5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11"))
                .willReturn(List.of(new Log(15, "5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11",
                        "I'll work on the weeks agenda", true, NOW, NOW)));

        mockMvc.perform(get("/worklog-api/v1/logs").principal(() -> "5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"data":{"items":[{"id":15,"content":"I'll work on the weeks agenda","active":true,
                         "createdAt":"2026-10-21T21:44:44Z","updatedAt":"2026-10-21T21:44:44Z"}]}}
                        """, JsonCompareMode.STRICT));
    }

    @Test
    void answers200WithEmptyItemsWhenNothingIsFoundForTheAccount() throws Exception {
        given(logUseCase.listActive("5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11")).willReturn(List.of());

        mockMvc.perform(get("/worklog-api/v1/logs").principal(() -> "5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items").isEmpty());
    }

    @Test
    void neverListsAnotherAccountsLogsEvenWhenTheRequestAsksForThem() throws Exception {
        String owner = "5f0c3c1e-8a52-4d0b-9a0e-3f6f2d9d7a11";
        String anotherAccount = "9b1d6c52-3e47-4a1f-8c60-0d2e7f5a4b33";
        given(logUseCase.listActive(owner))
                .willReturn(List.of(new Log(15, owner, "my own log", true, NOW, NOW)));
        given(logUseCase.listActive(anotherAccount))
                .willReturn(List.of(new Log(16, anotherAccount, "log of another account", true, NOW, NOW)));

        mockMvc.perform(get("/worklog-api/v1/logs")
                        .queryParam("userUUID", anotherAccount)
                        .principal(() -> owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(15))
                .andExpect(jsonPath("$.data.items[0].content").value("my own log"));

        then(logUseCase).should(never()).listActive(anotherAccount);
    }
}
