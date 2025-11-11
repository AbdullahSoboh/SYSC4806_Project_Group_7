package ca.carleton.s4806.perkmanager.repository;

import ca.carleton.s4806.perkmanager.model.Membership;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for MembershipRepository.
 * Tests the data access layer with an in-memory H2 database.
 *
 * @author Moesa Malik
 * @version 1.0
 */
@SpringBootTest
public class MembershipRepositoryTest {

    @Autowired
    private MembershipRepository membershipRepository;

    /**
     * Tests saving a membership to the database and retrieving it by ID.
     * Verifies that all fields are correctly persisted and retrieved.
     */
    @Test
    public void testSaveAndFind() {
        // Create a new Membership
        Membership membership = new Membership("Visa");

        // Save it to the database
        Membership savedMembership = membershipRepository.save(membership);

        // Verify that the membership was saved and got an ID assigned
        assertNotNull(savedMembership.getId());

        // Retrieve it using findById
        Optional<Membership> retrievedMembershipOptional = membershipRepository.findById(savedMembership.getId());

        // Verify that the membership was found
        assertTrue(retrievedMembershipOptional.isPresent());

        // Assert that the retrieved data matches what was saved
        Membership retrievedMembership = retrievedMembershipOptional.get();
        assertEquals("Visa", retrievedMembership.getName());
    }

    /**
     * Tests the findAll method by saving multiple memberships and verifying
     * that all saved memberships are returned.
     */
    @Test
    public void testFindAll() {
        // Clear any existing data
        membershipRepository.deleteAll();

        // Create and save multiple memberships
        Membership membership1 = new Membership("Air Miles");
        Membership membership2 = new Membership("CAA");

        membershipRepository.save(membership1);
        membershipRepository.save(membership2);

        // Verify that findAll returns both memberships
        assertEquals(2, membershipRepository.findAll().size());
    }
}