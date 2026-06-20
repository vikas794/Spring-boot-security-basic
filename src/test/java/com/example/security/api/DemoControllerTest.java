package com.example.security.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void publicEndpoint_ShouldBeAccessibleByAnyone() throws Exception {
        mockMvc.perform(get("/api/demo/public"))
                .andExpect(status().isOk())
                .andExpect(content().string("This is a public endpoint. Anyone can access it."));
    }

    @Test
    public void protectedEndpoint_ShouldBeUnauthorizedForAnonymous() throws Exception {
        mockMvc.perform(get("/api/demo/protected"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void protectedEndpoint_ShouldBeAccessibleByAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/demo/protected"))
                .andExpect(status().isOk())
                .andExpect(content().string("This is a protected endpoint. Any authenticated user can access it."));
    }

    @Test
    public void adminEndpoint_ShouldBeUnauthorizedForAnonymous() throws Exception {
        mockMvc.perform(get("/api/demo/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void adminEndpoint_ShouldBeForbiddenForNormalUser() throws Exception {
        mockMvc.perform(get("/api/demo/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void adminEndpoint_ShouldBeAccessibleByAdmin() throws Exception {
        mockMvc.perform(get("/api/demo/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("This is an admin endpoint. Only ADMINs can access it."));
    }

    @Test
    public void abacEndpoint_ShouldBeUnauthorizedForAnonymous() throws Exception {
        mockMvc.perform(get("/api/demo/resource/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user")
    public void abacEndpoint_ShouldBeAccessibleForValidResource() throws Exception {
        // Resource ID <= 10 should be accessible
        mockMvc.perform(get("/api/demo/resource/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("You have access to resource: 5"));
    }

    @Test
    @WithMockUser(username = "user")
    public void abacEndpoint_ShouldBeForbiddenForInvalidResource() throws Exception {
        // Resource ID > 10 should be forbidden for non-admin
        mockMvc.perform(get("/api/demo/resource/15"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void abacEndpoint_ShouldBeAccessibleForAnyResourceByAdmin() throws Exception {
        // Admin user can access any resource
        mockMvc.perform(get("/api/demo/resource/15"))
                .andExpect(status().isOk())
                .andExpect(content().string("You have access to resource: 15"));
    }
}
