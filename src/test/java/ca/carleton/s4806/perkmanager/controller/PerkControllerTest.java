package ca.carleton.s4806.perkmanager.controller;


import ca.carleton.s4806.perkmanager.model.Membership;
import ca.carleton.s4806.perkmanager.model.Perk;
import ca.carleton.s4806.perkmanager.repository.MembershipRepository;
import ca.carleton.s4806.perkmanager.repository.PerkRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for the PerkController.
 * <p>
 * This test uses @SpringBootTest to load the full application context
 * and @AutoConfigureMockMvc to set up a MockMvc instance for sending
 * requests to the controller without a running server.
 *
 * @author Tommy Csete, Imann Brar
 * @version 3.0
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PerkControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simulates HTTP requests to test Spring MVC controllers

    @Autowired
    private PerkRepository perkRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Membership testMembership;

    @BeforeEach
    public void setUp() {
        // Create a test membership that can be reused across tests
        testMembership = membershipRepository.save(new Membership("Visa"));
    }

    @AfterEach
    public void tearDown() {
        perkRepository.deleteAll();
        membershipRepository.deleteAll();
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
    public void testGetPerksEmpty() throws Exception {
        perkRepository.deleteAll(); // Ensure empty
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
    public void testGetPerksExisting() throws Exception {
        Perk perk1 = new Perk(
                "Movie Discount",
                "50% off general admission",
                "Movies",
                testMembership,
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
                .andExpect(jsonPath("$[0].product", is("Movies")))
                .andExpect(jsonPath("$[0].membership.name", is("Visa")));
    }

    /**
     * Happy path: POST /api/perks with a valid payload returns 201 Created
     * and echoes the saved perk including a generated id.
     */
    @Test
    public void testCreatePerk_CreatesAndReturns201() throws Exception {
        Membership gold = membershipRepository.save(new Membership("Gold"));

        Perk payload = new Perk(
                "Test Perk",
                "Test Desc",
                "Pro Plan",
                gold,
                LocalDate.of(2026, 12, 31),
                "Ottawa, ON"
        );
        // Intentionally leave votes null to test defaults in the controller
        payload.setUpvotes(null);
        payload.setDownvotes(null);

        String json = objectMapper.writeValueAsString(payload);

        mockMvc.perform(
                        post("/api/perks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Test Perk"))
                .andExpect(jsonPath("$.membership.name").value("Gold"))
                .andExpect(jsonPath("$.location").value("Ottawa, ON"))
                .andExpect(jsonPath("$.expiryDate").value("2026-12-31"));
    }

    /**
     * Verifies that if upvotes/downvotes are omitted or null, the controller
     * initializes them to 0 before saving.
     */
    @Test
    public void testCreatePerk_DefaultsVotesToZero() throws Exception {
        Membership any = membershipRepository.save(new Membership("Any"));

        Perk payload = new Perk(
                "Votes Default",
                "No votes provided",
                "Any",
                any,
                LocalDate.now().plusYears(1),
                "Ottawa, ON"
        );
        payload.setUpvotes(null);
        payload.setDownvotes(null);

        String json = objectMapper.writeValueAsString(payload);

        mockMvc.perform(
                        post("/api/perks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.upvotes").value(0))
                .andExpect(jsonPath("$.downvotes").value(0));
    }

    /**
     * Round-trip verification for create + read.
     * <p>
     * Goal: Ensure that a perk created via POST /api/perks
     * is actually persisted and then retrievable via GET /api/perks.
     * <p>
     * Given a valid JSON payload for a new perk
     * when the client posts it to the API and then immediately fetches all perks,
     * then the GET response must include the newly created perk with the expected fields.
     * <p>
     * Asserts
     * <p>
     * 201 Created on POST
     * 200 OK + application/json on GET
     * Array contains an item whose title and location match the POSTed data
     *
     * @author Imann Brar
     * @version 2.0
     */
    @Test
    public void testCreatePerk_ThenGetPerksContainsNewItem() throws Exception {
        Membership any = membershipRepository.save(new Membership("Any"));

        Perk payload = new Perk(
                "Round Trip",
                "Persist then fetch",
                "Any",
                any,
                LocalDate.of(2027, 1, 1),
                "Ottawa, ON"
        );
        payload.setUpvotes(null);
        payload.setDownvotes(null);

        String json = objectMapper.writeValueAsString(payload);

        // Create
        mockMvc.perform(
                        post("/api/perks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());

        // Fetch and verify it’s there
        mockMvc.perform(get("/api/perks"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Round Trip")))
                .andExpect(jsonPath("$[0].location", is("Ottawa, ON")));
    }

    @Test
    public void testGetPerksSearchFiltersResults() throws Exception {
        Membership visa = testMembership;
        Membership mc = membershipRepository.save(new Membership("MC"));
        Membership costco = membershipRepository.save(new Membership("Costco"));

        Perk matchTitle = new Perk("Movie Night", "Snacks", "Cinema", visa, LocalDate.now().plusMonths(2), "Ottawa, ON");
        Perk matchProduct = new Perk("Snacks Promo", "Discount", "Movie Tickets", mc, LocalDate.now().plusMonths(3), "Toronto, ON");
        Perk other = new Perk("Grocery Deal", "Food", "Groceries", costco, LocalDate.now().plusMonths(1), "Montreal, QC");

        perkRepository.saveAll(List.of(matchTitle, matchProduct, other));

        mockMvc.perform(get("/api/perks")
                        .param("search", "movie")
                        .param("sortBy", "title"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("Movie Night", "Snacks Promo")));
    }

    @Test
    public void testGetPerksSortsResults() throws Exception {
        Perk lowVotes = new Perk("Low Votes", "desc", "Movies", testMembership, LocalDate.now().plusMonths(1), "Ottawa, ON");
        lowVotes.setUpvotes(1);
        Perk highVotes = new Perk("High Votes", "desc", "Movies", testMembership, LocalDate.now().plusMonths(1), "Ottawa, ON");
        highVotes.setUpvotes(5);

        perkRepository.saveAll(List.of(lowVotes, highVotes));

        mockMvc.perform(get("/api/perks")
                        .param("sortBy", "upvotes")
                        .param("direction", "desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("High Votes")))
                .andExpect(jsonPath("$[1].title", is("Low Votes")));
    }

    /**
     * Server authority over identifiers: client-supplied id must be ignored.
     * <p>
     * Goal: Verify that the API does not trust or persist a client-provided primary key.
     * The server must generate its own identifier when persisting a new entity.
     * <p>
     * Given a POST request whose JSON includes id = 999
     * when the server creates the perk,
     * then the response must contain an auto-generated, positive id that is
     * not the client-supplied value.
     * <p>
     * Asserts:
     * <p>
     * 201 Created + JSON response
     * $.id ≠ 999
     * $.id; 0 (server-generated)
     * <p>
     * Rationale: Prevents clients from colliding with or spoofing primary keys, and
     * enforces the domain rule that identifiers are owned by the persistence layer.
     *
     * @author Imann Brar
     * @version 2.0
     */
    @Test
    public void testCreatePerk_IgnoresClientProvidedId() throws Exception {
        Membership any = membershipRepository.save(new Membership("Any"));

        Perk payload = new Perk(
                "Client Id Ignored",
                "Server must generate id",
                "Any",
                any,
                LocalDate.now().plusYears(1),
                "Ottawa, ON"
        );
        payload.setId(999L);             // Client tries to force an id
        payload.setUpvotes(null);
        payload.setDownvotes(null);

        String json = objectMapper.writeValueAsString(payload);

        mockMvc.perform(
                        post("/api/perks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(not(999)))       // not the client-provided id
                .andExpect(jsonPath("$.id").value(greaterThan(0))); // server-generated positive id
    }

    /**
     * Tests POST /api/perks/{id}/upvote for a valid perk.
     * Expects 200 OK and the upvote count to be incremented.
     */
    @Test
    public void testUpvotePerk_Success() throws Exception {
        Perk perk = new Perk("Test Upvote", "Desc", "Prod", testMembership, null, "Ottawa, ON");
        perk.setUpvotes(0);
        perk.setDownvotes(0);
        Perk savedPerk = perkRepository.save(perk);
        Long perkId = savedPerk.getId();

        mockMvc.perform(post("/api/perks/" + perkId + "/upvote"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(perkId.intValue())))
                .andExpect(jsonPath("$.upvotes", is(1)))
                .andExpect(jsonPath("$.downvotes", is(0)));
    }

    /**
     * Tests POST /api/perks/{id}/upvote for a non-existent perk.
     * Expects 404 Not Found.
     */
    @Test
    public void testUpvotePerk_NotFound() throws Exception {
        long nonExistentId = 999L;

        mockMvc.perform(post("/api/perks/" + nonExistentId + "/upvote"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    /**
     * Tests POST /api/perks/{id}/downvote for a valid perk.
     * Expects 200 OK and the downvote count to be incremented.
     */
    @Test
    public void testDownvotePerk_Success() throws Exception {
        Perk perk = new Perk("Test Downvote", "Desc", "Prod", testMembership, null, "Ottawa, ON");
        perk.setUpvotes(0);
        perk.setDownvotes(0);
        Perk savedPerk = perkRepository.save(perk);
        Long perkId = savedPerk.getId();

        mockMvc.perform(post("/api/perks/" + perkId + "/downvote"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(perkId.intValue())))
                .andExpect(jsonPath("$.upvotes", is(0)))
                .andExpect(jsonPath("$.downvotes", is(1)));
    }

    /**
     * Tests POST /api/perks/{id}/downvote for a non-existent perk.
     * Expects 404 Not Found.
     */
    @Test
    public void testDownvotePerk_NotFound() throws Exception {
        long nonExistentId = 999L;

        mockMvc.perform(post("/api/perks/" + nonExistentId + "/downvote"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    /**
     * Tests DELETE /api/perks/{id} for a valid perk.
     * Expects 204 No Content and verifies the perk is removed from the database.
     */
    @Test
    public void testDeletePerk_Success() throws Exception {
        Perk perk = new Perk("Perk to be Deleted", "Desc", "Prod", testMembership, null, "Ottawa, ON");
        Perk savedPerk = perkRepository.save(perk);
        Long perkId = savedPerk.getId();

        mockMvc.perform(delete("/api/perks/" + perkId))
                .andDo(print())
                .andExpect(status().isNoContent()); // 204

        assertFalse(
                perkRepository.findById(perkId).isPresent(),
                "Perk should be deleted from the database"
        );
    }

    /**
     * Tests DELETE /api/perks/{id} for a non-existent perk.
     * Expects 404 Not Found.
     */
    @Test
    public void testDeletePerk_NotFound() throws Exception {
        long nonExistentId = 999L;

        mockMvc.perform(delete("/api/perks/" + nonExistentId))
                .andDo(print())
                .andExpect(status().isNotFound()); // 404
    }
}