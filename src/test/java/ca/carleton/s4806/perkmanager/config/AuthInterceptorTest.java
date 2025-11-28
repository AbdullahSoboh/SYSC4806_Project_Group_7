package ca.carleton.s4806.perkmanager.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthInterceptorTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProtectedController())
                .addInterceptors(new AuthInterceptor())
                .build();
    }

    @Test
    void postWithoutSessionIsUnauthorized() throws Exception {
        mockMvc.perform(post("/protected"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postWithSessionSucceeds() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", new Object());

        mockMvc.perform(post("/protected").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void getBypassesInterceptor() throws Exception {
        mockMvc.perform(get("/protected"))
                .andExpect(status().isOk());
    }

    @RestController
    @RequestMapping("/protected")
    static class ProtectedController {
        @PostMapping
        public ResponseEntity<Void> create() {
            return ResponseEntity.ok().build();
        }

        @GetMapping
        public ResponseEntity<Void> read() {
            return ResponseEntity.ok().build();
        }
    }
}
