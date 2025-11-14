package ca.carleton.s4806.perkmanager.controller;

import ca.carleton.s4806.perkmanager.model.Membership;
import ca.carleton.s4806.perkmanager.repository.MembershipRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST controller exposing membership data for the frontend.
 */
@RestController
@CrossOrigin
@RequestMapping("/api/memberships")
public class MembershipController {

    private final MembershipRepository membershipRepository;

    public MembershipController(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    /**
     * Returns all memberships sorted alphabetically by name.
     *
     * @return list of memberships
     */
    @GetMapping
    public List<Membership> getMemberships() {
        return membershipRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    /**
     * Creates a new membership.
     *
     * @param membership membership payload from client
     * @return persisted membership
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Membership createMembership(@RequestBody Membership membership) {
        String name = membership.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Membership name is required");
        }
        membership.setId(null);
        String normalizedName = name.trim();
        if (membershipRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Membership already exists");
        }
        membership.setName(normalizedName);
        return membershipRepository.save(membership);
    }
}
