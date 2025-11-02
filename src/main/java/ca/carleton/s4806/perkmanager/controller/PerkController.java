package ca.carleton.s4806.perkmanager.controller;

import ca.carleton.s4806.perkmanager.model.Perk;
import ca.carleton.s4806.perkmanager.repository.PerkRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;  // <-- add PostMapping, RequestBody, ResponseStatus
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing Perks.
 *
 * Provides API endpoints under the "/api/perks" path.
 * Allows Cross-Origin requests for frontend integration.
 *
 * @author Tommy Csete , Imann Brar
 * @version 1.0
 */

@RestController
@CrossOrigin
@RequestMapping("/api/perks")
public class PerkController {

    private final PerkRepository perkRepository; // Repository for Perk Data operations

    /**
     * Constructs the controller and injects the PerkRepository.
     *
     * @param perkRepository The repository implementation provided by Spring.
     */
    public PerkController(PerkRepository perkRepository) {
        this.perkRepository = perkRepository;
    }

    /**
     * Gets a list of all perks.
     * Responds to HTTP GET requests on "/api/perks".
     *
     * @return A List of all Perk Objects (serialized as JSON)
     */
    @GetMapping
    public List<Perk> getAllPerks() {
        return perkRepository.findAll();

    }
    /**
     * Creates a new {@link Perk}.
     *
     * Accepts a JSON request body that maps to the {@link Perk} fields.
     * If the client omits vote counters, they are initialized to zero.
     * On success, the saved entity (including its generated {@code id}) is returned.</p>
     *
     * Example request:
     *
     * POST /api/perks
     * Content-Type: application/json
     *
     * {
     *   "title": "Student Discount",
     *   "description": "15% off with student ID",
     *   "product": "Movie Tickets",
     *   "membership": "University",
     *   "location": "Ottawa, ON",
     *   "expiryDate": "2026-12-31"
     * }
     *
     *
     * @param perk the perk data sent by the client
     * @return the persisted perk with its generated ID
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201
    public Perk createPerk(@RequestBody Perk perk) {
        // ensure counters default to 0 if omitted by client
        if (perk.getUpvotes() == null) perk.setUpvotes(0);
        if (perk.getDownvotes() == null) perk.setDownvotes(0);
        return perkRepository.save(perk);
    }
}
