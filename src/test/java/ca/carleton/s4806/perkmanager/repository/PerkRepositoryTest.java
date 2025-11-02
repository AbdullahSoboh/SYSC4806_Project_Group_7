package ca.carleton.s4806.perkmanager.repository;

import ca.carleton.s4806.perkmanager.model.Perk;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PerkRepository.
 * Tests the data access layer with an in-memory H2 database.
 *
 * @author Moesa Malik
 * @version 1.0
 */
@SpringBootTest
public class PerkRepositoryTest {

    @Autowired
    private PerkRepository perkRepository;

    /**
     * Tests saving a perk to the database and retrieving it by ID.
     * Verifies that all fields are correctly persisted and retrieved.
     */
    @Test
    public void testSaveAndFind() {
        // Create a new Perk object and set its data
        Perk perk = new Perk();
        perk.setTitle("10% off Movies");
        perk.setDescription("Get 10% discount on movie tickets");
        perk.setProduct("Movie Tickets");
        perk.setMembership("Visa Card");
        perk.setUpvotes(5);
        perk.setExpiryDate(LocalDate.of(2025, 12, 31));

        // Save it to the database
        Perk savedPerk = perkRepository.save(perk);

        // Verify that the perk was saved and got an ID assigned
        assertNotNull(savedPerk.getId());

        // Retrieve it using findById
        Optional<Perk> retrievedPerkOptional = perkRepository.findById(savedPerk.getId());

        // Verify that the perk was found
        assertTrue(retrievedPerkOptional.isPresent());

        // Assert that the retrieved data matches what was saved
        Perk retrievedPerk = retrievedPerkOptional.get();
        assertEquals("10% off Movies", retrievedPerk.getTitle());
        assertEquals("Get 10% discount on movie tickets", retrievedPerk.getDescription());
        assertEquals("Movie Tickets", retrievedPerk.getProduct());
        assertEquals("Visa Card", retrievedPerk.getMembership());
        assertEquals(5, retrievedPerk.getUpvotes());
        assertEquals(LocalDate.of(2025, 12, 31), retrievedPerk.getExpiryDate());
    }

    /**
     * Tests the findAll method by saving multiple perks and verifying
     * that all saved perks are returned.
     */
    @Test
    public void testFindAll() {
        // Clear any existing data
        perkRepository.deleteAll();

        // Create and save multiple perks
        Perk perk1 = new Perk("Free Flight", "Free domestic flight", "Flight", "Air Miles", LocalDate.of(2025, 11, 30));
        Perk perk2 = new Perk("Roadside Assistance", "Free towing service", "Towing", "CAA", LocalDate.of(2026, 1, 15));

        perkRepository.save(perk1);
        perkRepository.save(perk2);

        // Verify that findAll returns both perks
        assertEquals(2, perkRepository.findAll().size());
    }
}