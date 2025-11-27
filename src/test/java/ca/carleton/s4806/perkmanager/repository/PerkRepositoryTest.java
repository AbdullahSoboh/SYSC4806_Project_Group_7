package ca.carleton.s4806.perkmanager.repository;

import ca.carleton.s4806.perkmanager.model.Membership;
import ca.carleton.s4806.perkmanager.model.Perk;
import ca.carleton.s4806.perkmanager.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PerkRepository.
 * Tests the data access layer with an in-memory H2 database.
 *
 * @author Moesa Malik
 * @version 3.0
 */
@SpringBootTest
public class PerkRepositoryTest {

    @Autowired
    private PerkRepository perkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @AfterEach
    public void tearDown() {
        perkRepository.deleteAll();
        userRepository.deleteAll();
        membershipRepository.deleteAll();
    }

    /**
     * Tests saving a perk to the database and retrieving it by ID.
     * Verifies that all fields are correctly persisted and retrieved.
     */
    @Test
    public void testSaveAndFind() {
        // Create and save a membership first (required for foreign key)
        Membership visa = membershipRepository.save(new Membership("Visa Card"));

        // Create a new Perk object and set its data
        Perk perk = new Perk();
        perk.setTitle("10% off Movies");
        perk.setDescription("Get 10% discount on movie tickets");
        perk.setProduct("Movie Tickets");
        perk.setMembership(visa);
        perk.setUpvotes(5);
        perk.setExpiryDate(LocalDate.of(2025, 12, 31));
        perk.setLocation("Ottawa, ON");

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
        assertNotNull(retrievedPerk.getMembership());
        assertEquals("Visa Card", retrievedPerk.getMembership().getName());
        assertEquals(5, retrievedPerk.getUpvotes());
        assertEquals(LocalDate.of(2025, 12, 31), retrievedPerk.getExpiryDate());
        assertEquals("Ottawa, ON", retrievedPerk.getLocation());
    }

    /**
     * Tests the findAll method by saving multiple perks and verifying
     * that all saved perks are returned.
     */
    @Test
    public void testFindAll() {
        // Clear any existing data
        perkRepository.deleteAll();
        userRepository.deleteAll();
        membershipRepository.deleteAll();

        // Create and save memberships first
        Membership airMiles = membershipRepository.save(new Membership("Air Miles"));
        Membership caa = membershipRepository.save(new Membership("CAA"));

        // Create and save multiple perks
        Perk perk1 = new Perk("Free Flight", "Free domestic flight", "Flight", airMiles, LocalDate.of(2025, 11, 30), "Toronto, ON");
        Perk perk2 = new Perk("Roadside Assistance", "Free towing service", "Towing", caa, LocalDate.of(2026, 1, 15), "Ottawa, ON");

        perkRepository.save(perk1);
        perkRepository.save(perk2);

        // Verify that findAll returns both perks
        assertEquals(2, perkRepository.findAll().size());
    }

    @Test
    public void testSearchByTitleOrProduct() {
        perkRepository.deleteAll();
        userRepository.deleteAll();
        membershipRepository.deleteAll();

        Membership visa = membershipRepository.save(new Membership("Visa"));
        Membership master = membershipRepository.save(new Membership("Mastercard"));
        Membership costco = membershipRepository.save(new Membership("Costco"));

        Perk movieTitle = new Perk("Movie Night", "Discounted tickets", "Movies", visa, LocalDate.now().plusDays(30), "Ottawa, ON");
        Perk productMatch = new Perk("Summer Blockbuster", "Best seats", "Movie Tickets", master, LocalDate.now().plusDays(60), "Toronto, ON");
        Perk nonMatch = new Perk("Grocery Deal", "Weekly savings", "Groceries", costco, LocalDate.now().plusDays(15), "Montreal, QC");

        perkRepository.save(movieTitle);
        perkRepository.save(productMatch);
        perkRepository.save(nonMatch);

        List<Perk> results = perkRepository
                .findByTitleContainingIgnoreCaseOrProductContainingIgnoreCase("movie", "movie");

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(perk -> perk.getTitle().equals("Movie Night")));
        assertTrue(results.stream().anyMatch(perk -> perk.getTitle().equals("Summer Blockbuster")));
    }

    @Test
    public void testSearchWithSorting() {
        perkRepository.deleteAll();
        userRepository.deleteAll();
        membershipRepository.deleteAll();

        Membership visa = membershipRepository.save(new Membership("Visa"));

        Perk lowVotes = new Perk("Movie Discount", "Save big", "Movies", visa, LocalDate.now().plusDays(30), "Ottawa, ON");
        lowVotes.setUpvotes(1);
        Perk highVotes = new Perk("Cinema Deal", "Even bigger savings", "Movies", visa, LocalDate.now().plusDays(30), "Ottawa, ON");
        highVotes.setUpvotes(5);

        perkRepository.save(lowVotes);
        perkRepository.save(highVotes);

        List<Perk> results = perkRepository
                .findByTitleContainingIgnoreCaseOrProductContainingIgnoreCase(
                        "movie",
                        "movie",
                        Sort.by(Sort.Direction.DESC, "upvotes")
                );

        assertEquals(2, results.size());
        assertEquals("Cinema Deal", results.get(0).getTitle());
        assertEquals("Movie Discount", results.get(1).getTitle());
    }
}
