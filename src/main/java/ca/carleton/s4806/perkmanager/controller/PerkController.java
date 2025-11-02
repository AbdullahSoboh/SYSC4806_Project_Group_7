package ca.carleton.s4806.perkmanager.controller;

import ca.carleton.s4806.perkmanager.model.Perk;
import ca.carleton.s4806.perkmanager.repository.PerkRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing Perks.
 * <p>
 * Provides API endpoints under the "/api/perks" path.
 * Allows Cross-Origin requests for frontend integration.
 *
 * @author Tommy Csete
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
}
