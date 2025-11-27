package ca.carleton.s4806.perkmanager.repository;

import ca.carleton.s4806.perkmanager.model.Membership;
import ca.carleton.s4806.perkmanager.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserRepository.
 * Tests the data access layer with an in-memory H2 database.
 * @author Moesa Malik
 * @version 1.0
 */
@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private PerkRepository perkRepository;

    @AfterEach
    public void tearDown() {
        // Clean up in correct order to avoid FK constraints
        perkRepository.deleteAll();
        userRepository.deleteAll();
        membershipRepository.deleteAll();
    }

    /**
     * Tests saving a user to the database and retrieving it by ID.
     */
    @Test
    public void testSaveAndFind() {
        User user = new User();
        user.setUsername("saveUser");
        user.setPassword("pass");
        user.setEmail("save@test.com");

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());

        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());
        assertTrue(retrievedUser.isPresent());
        assertEquals("saveUser", retrievedUser.get().getUsername());
    }

    /**
     * Tests finding a user by username.
     */
    @Test
    public void testFindByUsername() {
        User user = new User();
        user.setUsername("uniqueName");
        user.setPassword("pass");
        user.setEmail("unique@test.com");

        userRepository.save(user);

        User foundUser = userRepository.findByUsername("uniqueName");
        assertNotNull(foundUser);
        assertEquals("unique@test.com", foundUser.getEmail());
    }

    /**
     * Tests saving a user with memberships (Many-to-Many relationship).
     */
    @Test
    public void testSaveUserWithMemberships() {
        Membership m1 = membershipRepository.save(new Membership("Gym"));
        Membership m2 = membershipRepository.save(new Membership("Library"));

        User user = new User();
        user.setUsername("memberUser");
        user.setPassword("pass");
        user.setEmail("member@test.com");
        user.addMembership(m1);
        user.addMembership(m2);

        User savedUser = userRepository.save(user);

        User retrievedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertEquals(2, retrievedUser.getMemberships().size());
        assertTrue(retrievedUser.getMemberships().stream().anyMatch(m -> m.getName().equals("Gym")));
        assertTrue(retrievedUser.getMemberships().stream().anyMatch(m -> m.getName().equals("Library")));
    }
}
