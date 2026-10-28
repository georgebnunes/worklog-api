package dev.bnunes.worklogapi.adapter.in.web;

import dev.bnunes.worklogapi.application.port.in.RegisterLogUseCase;
import dev.bnunes.worklogapi.domain.model.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogController.class)
class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterLogUseCase registerLog;

    @Test
    void passesTheBearerTokenAndContentToTheUseCaseAndAnswers201WithTheLog() throws Exception {
        Instant now = Instant.parse("2026-10-21T21:44:44Z");
        given(registerLog.register("a.b.c", "lorem ipsum dolor sit amet"))
                .willReturn(new Log(1, "lorem ipsum dolor sit amet", 3, true, now, now));

        mockMvc.perform(post("/worklog-api/v1/logs")
                        .header("Authorization", "Bearer a.b.c")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"lorem ipsum dolor sit amet"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("lorem ipsum dolor sit amet"))
                .andExpect(jsonPath("$.userId").value(3))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").value("2026-10-21T21:44:44Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-10-21T21:44:44Z"));
    }
}
