package ca.carleton.s4806.perkmanager.controller;

import ca.carleton.s4806.perkmanager.model.Perk;
import ca.carleton.s4806.perkmanager.repository.PerkRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing Perks.
 * <p>
 * Provides API endpoints under the "/api/perks" path.
 * Allows Cross-Origin requests for frontend integration.
 *
 * @author Tommy Csete , Imann Brar
 * @version 2.0
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
            Map.entry("location", "location"),
            Map.entry("score", "votes")
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
        boolean sortByScore = isScoreSort(sortBy);
        Sort sort = sortByScore ? Sort.unsorted() : resolveSort(sortBy, direction);
        boolean hasSearch = searchKeyword != null && !searchKeyword.trim().isEmpty();
        List<Perk> perks;

        if (hasSearch) {
            if (sort.isUnsorted()) {
                perks = perkRepository.findByTitleContainingIgnoreCaseOrProductContainingIgnoreCase(
                        searchKeyword,
                        searchKeyword
                );
            } else {
                perks = perkRepository.findByTitleContainingIgnoreCaseOrProductContainingIgnoreCase(
                        searchKeyword,
                        searchKeyword,
                        sort
                );
            }
        } else {
            perks = sort.isUnsorted()
                    ? perkRepository.findAll()
                    : perkRepository.findAll(sort);
        }

        if (sortByScore) {
            sortPerksByScore(perks, direction);
        }

        return perks;

    }

    /**
     * Upvotes a perk.
     * Responds to HTTP POST requests on "/api/perks/{id}/upvote".
     *
     * @param id The ID of the perk to upvote
     * @return The updated Perk object
     */
    @PostMapping("/{id}/upvote")
    public Perk upvotePerk(@PathVariable Long id) {
        Perk perk = perkRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perk not found"));

        Integer currentUpvotes = perk.getUpvotes();
        perk.setUpvotes((currentUpvotes == null ? 0 : currentUpvotes) + 1);
        perk.setVotes(perk.getUpvotes() - perk.getDownvotes());

        return perkRepository.save(perk);
    }

    /**
     * Downvotes a perk.
     * Responds to HTTP POST requests on "/api/perks/{id}/downvote".
     *
     * @param id The ID of the perk to downvote
     * @return The updated Perk object
     */
    @PostMapping("/{id}/downvote")
    public Perk downvotePerk(@PathVariable Long id) {
        Perk perk = perkRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perk not found"));

        Integer currentDownvotes = perk.getDownvotes();
        perk.setDownvotes((currentDownvotes == null ? 0 : currentDownvotes) + 1);
        perk.setVotes(perk.getUpvotes() - perk.getDownvotes());

        return perkRepository.save(perk);
    }

    /**
     * Creates a new {@link Perk}.
     * <p>
     * Accepts a JSON request body that maps to the {@link Perk} fields.
     * If the client omits vote counters, they are initialized to zero.
     * On success, the saved entity (including its generated {@code id}) is returned.</p>
     * <p>
     * Example request:
     * <p>
     * POST /api/perks
     * Content-Type: application/json
     * <p>
     * {
     * "title": "Student Discount",
     * "description": "15% off with student ID",
     * "product": "Movie Tickets",
     * "membership": "University",
     * "location": "Ottawa, ON",
     * "expiryDate": "2026-12-31"
     * }
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

        if (perk.getExpiryDate() != null) {
            LocalDate today = LocalDate.now();
            if (perk.getExpiryDate().isBefore(today)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Expiry date cannot be in the past."
                );
            }
        }
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

    private boolean isScoreSort(String sortBy) {
        return sortBy != null && sortBy.trim().equalsIgnoreCase("score");
    }

    private void sortPerksByScore(List<Perk> perks, String direction) {
        if (perks == null || perks.size() < 2) {
            return;
        }

        Comparator<Perk> comparator = Comparator.comparingInt(Perk::getScore);
        if (direction != null && direction.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }
        perks.sort(comparator);
    }
}
