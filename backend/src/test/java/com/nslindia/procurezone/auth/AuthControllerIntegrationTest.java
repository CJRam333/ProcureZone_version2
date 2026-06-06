package com.nslindia.procurezone.auth;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_withValidCredentials_returnsAccessTokenAndUserDetails() throws Exception {
        // username = emp_email (employee-only auth — tbl_user_master is bypassed)
        String payload = """
                {
                  \"username\": \"legacy.user@nsl.com\",
                  \"password\": \"password123\"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", equalTo("Bearer")))
                .andExpect(jsonPath("$.user.userId", equalTo(2001)))  // userId = emp_number (was user_id=1001)
                .andExpect(jsonPath("$.user.employeeNumber", equalTo(2001)))
                .andExpect(jsonPath("$.user.employeeId", equalTo("E-1001")))
                .andExpect(jsonPath("$.user.displayName", equalTo("Legacy User")))
                .andExpect(jsonPath("$.user.email", equalTo("legacy.user@nsl.com")))
                .andExpect(jsonPath("$.user.roles[0]", equalTo("ADMIN")));
    }

    @Test
    void login_withInvalidCredentials_returnsUnauthorized() throws Exception {
        // Correct email but wrong password — expects 401
        String payload = """
                {
                  \"username\": \"legacy.user@nsl.com\",
                  \"password\": \"wrong-password\"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", equalTo("AUTH_INVALID_CREDENTIALS")))
                .andExpect(jsonPath("$.message", equalTo("Invalid username or password")));
    }
}
