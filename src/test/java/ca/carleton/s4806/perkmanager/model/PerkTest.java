package ca.carleton.s4806.perkmanager.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * Unit tests covering {@link Perk} constructor defaults, mutators, and score logic.
 */
class PerkTest {

    /**
     * Verifies the constructor populates fields and defaults votes to zero.
     */
    @Test
    void constructorInitializesCoreFields() {
        LocalDate expiry = LocalDate.of(2025, 12, 31);
        Membership membership = new Membership("Visa");

        Perk perk = new Perk("Title", "Description", "Product", membership, expiry, "Ottawa");

        assertNull(perk.getId(), "JPA-managed id should default to null before persistence");
        assertEquals("Title", perk.getTitle());
        assertEquals("Description", perk.getDescription());
        assertEquals("Product", perk.getProduct());
        assertNotNull(perk.getMembership());
        assertEquals("Visa", perk.getMembership().getName());
        assertEquals("Ottawa", perk.getLocation());
        assertEquals(expiry, perk.getExpiryDate());
        assertEquals(0, perk.getUpvotes());
        assertEquals(0, perk.getDownvotes());
        assertEquals(0, perk.getScore());
    }

    /**
     * Ensures setters override every mutable field exposed by the model.
     */
    @Test
    void settersUpdateMutableFields() {
        Perk perk = new Perk();
        LocalDate expiry = LocalDate.of(2030, 1, 1);
        Membership membership = new Membership("Mastercard");

        perk.setId(42L);
        perk.setTitle("Updated Title");
        perk.setDescription("Updated Description");
        perk.setProduct("Updated Product");
        perk.setMembership(membership);
        perk.setLocation("Toronto");
        perk.setExpiryDate(expiry);
        perk.setUpvotes(7);
        perk.setDownvotes(2);

        assertEquals(42L, perk.getId());
        assertEquals("Updated Title", perk.getTitle());
        assertEquals("Updated Description", perk.getDescription());
        assertEquals("Updated Product", perk.getProduct());
        assertNotNull(perk.getMembership());
        assertEquals("Mastercard", perk.getMembership().getName());
        assertEquals("Toronto", perk.getLocation());
        assertEquals(expiry, perk.getExpiryDate());
        assertEquals(7, perk.getUpvotes());
        assertEquals(2, perk.getDownvotes());
    }

    /**
     * Confirms the computed score reflects the balance between upvotes and downvotes.
     */
    @Test
    void getScoreReflectsVoteBalance() {
        Perk perk = new Perk();
        perk.setUpvotes(10);
        perk.setDownvotes(4);

        assertEquals(6, perk.getScore());

        perk.setDownvotes(12);
        assertEquals(-2, perk.getScore());
    }
}