package ca.carleton.s4806.perkmanager.controller;

import ca.carleton.s4806.perkmanager.model.LoginRequest;
import ca.carleton.s4806.perkmanager.model.User;
import ca.carleton.s4806.perkmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @Test
    void loginSetsSessionAndReturnsOkWhenCredentialsMatch() throws Exception {
        User user = new User("alice", "secret", "alice@example.com", java.util.List.of());
        LoginRequest request = new LoginRequest("alice", "secret");

        when(userRepository.findByUsername("alice")).thenReturn(user);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("user", user));
    }

    @Test
    void loginReturnsUnauthorizedWhenUserNotFound() throws Exception {
        LoginRequest request = new LoginRequest("missing", "nopass");

        when(userRepository.findByUsername("missing")).thenReturn(null);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturnsUnauthorizedWhenPasswordDoesNotMatch() throws Exception {
        User user = new User("bob", "other", "bob@example.com", java.util.List.of());
        LoginRequest request = new LoginRequest("bob", "wrong");

        when(userRepository.findByUsername("bob")).thenReturn(user);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", new User("bob", "pass", "bob@example.com", java.util.List.of()));

        MvcResult result = mockMvc.perform(post("/api/logout").session(session))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(session.isInvalid()).isTrue();
        assertThat(result.getRequest().getSession(false)).isNull();
    }
}
