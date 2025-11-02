package ca.carleton.s4806.perkmanager.controller;

import ca.carleton.s4806.perkmanager.model.Perk;
import ca.carleton.s4806.perkmanager.repository.PerkRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for the PerkController.
 * <p>
 * This test uses @SpringBootTest to load the full application context
 * and @AutoConfigureMockMvc to set up a MockMvc instance for sending
 * requests to the controller without a running server.
 *
 * @author Tommy Csete
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PerkControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simulates HTTP requests to test Spring MVC controllers

    @Autowired
    private PerkRepository perkRepository;

    @AfterEach
    public void tearDown() {
        perkRepository.deleteAll();
    }

    /**
     * Tests the GET /api/perks endpoint.
     * <p>
     * It performs a GET request and verifies that the response has:
     * 1. An HTTP 200 (OK) status.
     * 2. A content type of "application/json".
     *
     * @throws Exception if the mockMvc.perform() call fails
     */
    @Test
    public void testGetPerks() throws Exception {
        this.mockMvc.perform(get("/api/perks"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Tests that GET /api/perks returns an empty JSON array
     * when no perks exist in the database.
     */
    @Test
    public void testGetAllPerksEmpty() throws Exception {
        mockMvc.perform(get("/api/perks"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    /**
     * Tests that GET /api/perks returns a list of perks
     * when perks do exist in the database.
     */
    @Test
    public void testGetAllPerksExisting() throws Exception {
        Perk perk1 = new Perk(
                "Movie Discount",
                "50% off general admission",
                "Movies",
                "Visa",
                LocalDate.now().plusYears(1),
                "Ottawa, ON"
        );

        perkRepository.save(perk1);

        mockMvc.perform(get("/api/perks"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Movie Discount")))
                .andExpect(jsonPath("$[0].product", is("Movies")));
    }
}
