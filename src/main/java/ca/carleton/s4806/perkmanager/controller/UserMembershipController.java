package ca.carleton.s4806.perkmanager.controller;

import ca.carleton.s4806.perkmanager.model.Membership;
import ca.carleton.s4806.perkmanager.model.User;
import ca.carleton.s4806.perkmanager.repository.MembershipRepository;
import ca.carleton.s4806.perkmanager.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserMembershipController {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    public UserMembershipController(UserRepository userRepository,
                                    MembershipRepository membershipRepository) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
    }

    public static class MembershipUpdateRequest {
        private List<Long> membershipIds;
        public List<Long> getMembershipIds() { return membershipIds; }
        public void setMembershipIds(List<Long> membershipIds) { this.membershipIds = membershipIds; }
    }

    @PutMapping("/memberships")
    public ResponseEntity<User> updateMemberships(@RequestBody MembershipUpdateRequest body,
                                                  HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Long> ids = body.getMembershipIds();
        if (ids == null) ids = Collections.emptyList();

        List<Membership> newMemberships =
                membershipRepository.findAllById(ids);

        // Re-fetch user from database to ensure we have the managed entity
        user = userRepository.findById(user.getId()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        user.setMemberships(newMemberships);
        User saved = userRepository.save(user);
        session.setAttribute("user", saved);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/memberships")
    public ResponseEntity<List<Membership>> getUserMemberships(HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Refresh from DB (important)
        User fresh = userRepository.findById(user.getId()).orElse(null);
        if (fresh == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(fresh.getMemberships());
    }
}
