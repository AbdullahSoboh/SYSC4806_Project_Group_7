package ca.carleton.s4806.perkmanager.controller;

import ca.carleton.s4806.perkmanager.model.Perk;
import ca.carleton.s4806.perkmanager.repository.PerkRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

    private static final Map<String, String> SORTABLE_FIELDS = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("title", "title"),
            Map.entry("product", "product"),
            Map.entry("membership", "membership.name"),
            Map.entry("membership.name", "membership.name"),
            Map.entry("membershipname", "membership.name"),
            Map.entry("upvotes", "upvotes"),
            Map.entry("downvotes", "downvotes"),
            Map.entry("expirydate", "expiryDate"),
            Map.entry("location", "location")
    );

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
    public List<Perk> getAllPerks(
            @RequestParam(value = "search", required = false) String searchKeyword,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "direction", required = false) String direction
    ) {
        Sort sort = resolveSort(sortBy, direction);
        boolean hasSearch = searchKeyword != null && !searchKeyword.trim().isEmpty();

        if (hasSearch) {
            if (sort.isUnsorted()) {
                return perkRepository.findByTitleContainingIgnoreCaseOrProductContainingIgnoreCase(
                        searchKeyword,
                        searchKeyword
                );
            }
            return perkRepository.findByTitleContainingIgnoreCaseOrProductContainingIgnoreCase(
                    searchKeyword,
                    searchKeyword,
                    sort
            );
        }

        if (sort.isUnsorted()) {
            return perkRepository.findAll();
        }
        return perkRepository.findAll(sort);

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
        // Force INSERT semantics even if the client sends an id
        perk.setId(null);
        // ensure counters default to 0 if omitted by client
        if (perk.getUpvotes() == null) perk.setUpvotes(0);
        if (perk.getDownvotes() == null) perk.setDownvotes(0);
        return perkRepository.save(perk);
    }

    private Sort resolveSort(String sortBy, String direction) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.unsorted();
        }
        String key = sortBy.toLowerCase();
        String property = SORTABLE_FIELDS.get(key);
        if (property == null) {
            return Sort.unsorted();
        }

        Sort.Direction sortDirection =
                (direction != null && direction.equalsIgnoreCase("desc"))
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        return Sort.by(sortDirection, property);
    }
}
