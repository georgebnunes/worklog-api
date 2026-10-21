package dev.bnunes.worklogapi.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Register an account")
class RegisterAccountAcceptanceIT {

    private static final String ACCOUNTS_PATH = "/worklog-api/v1/accounts";

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Must create an account from `username`, `password` and `email`, and answer 201 with `id`, `username` and `email`.")
    class CreateAccount {

        @Test
        @DisplayName("The one where all three fields are valid and unused. The account is saved and the response is 201 without the password.")
        void allThreeFieldsValidAndUnused() throws Exception {
            mockMvc.perform(post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"user1","password":"password123","email":"bob@bob.com"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.username").value("user1"))
                    .andExpect(jsonPath("$.email").value("bob@bob.com"))
                    .andExpect(jsonPath("$.password").doesNotExist());
        }

        @Test
        @DisplayName("The one where the request also sends `active` or `emailConfirmed`. They are ignored.")
        void activeAndEmailConfirmedAreIgnored() throws Exception {
            mockMvc.perform(post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"user2","password":"password123","email":"alice@alice.com",
                                     "active":false,"emailConfirmed":true}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.username").value("user2"))
                    .andExpect(jsonPath("$.email").value("alice@alice.com"))
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.active").doesNotExist())
                    .andExpect(jsonPath("$.emailConfirmed").doesNotExist());
        }
    }

    @Nested
    @DisplayName("Must reject a username or email that is already registered, ignoring case (409 Conflict).")
    class DuplicateAccount {

        @Test
        @DisplayName("The one where `User3` is registered and `user3` is submitted. The result is 409.")
        void usernameAlreadyRegisteredIgnoringCase() throws Exception {
            mockMvc.perform(post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"User3","password":"password123","email":"carol@carol.com"}
                                    """))
                    .andExpect(status().isCreated());

            mockMvc.perform(post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"user3","password":"password123","email":"dave@dave.com"}
                                    """))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("The one where `Erin@Erin.com` is registered and `erin@erin.com` is submitted under another username. The result is 409.")
        void emailAlreadyRegisteredIgnoringCase() throws Exception {
            mockMvc.perform(post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"user4","password":"password123","email":"Erin@Erin.com"}
                                    """))
                    .andExpect(status().isCreated());

            mockMvc.perform(post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"user5","password":"password123","email":"erin@erin.com"}
                                    """))
                    .andExpect(status().isConflict());
        }
    }
}
