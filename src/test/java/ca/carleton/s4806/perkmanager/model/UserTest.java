package ca.carleton.s4806.perkmanager.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests covering User constructor defaults, mutators, and membership management.
 * @author Moesa Malik
 * @version 1.0
 */
class UserTest {

    /**
     * Verifies the constructor populates fields.
     */
    @Test
    void constructorInitializesCoreFields() {
        List<Membership> memberships = new ArrayList<>();
        memberships.add(new Membership("Visa"));

        User user = new User("testuser", "password123", "test@example.com", memberships);

        assertNull(user.getId(), "JPA-managed id should default to null before persistence");
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(1, user.getMemberships().size());
        assertEquals("Visa", user.getMemberships().get(0).getName());
    }

    /**
     * Ensures setters update the fields correctly.
     */
    @Test
    void settersUpdateMutableFields() {
        User user = new User();
        List<Membership> memberships = new ArrayList<>();
        memberships.add(new Membership("Mastercard"));

        user.setId(100L);
        user.setUsername("updatedUser");
        user.setPassword("newPass");
        user.setEmail("new@example.com");
        user.setMemberships(memberships);

        assertEquals(100L, user.getId());
        assertEquals("updatedUser", user.getUsername());
        assertEquals("newPass", user.getPassword());
        assertEquals("new@example.com", user.getEmail());
        assertEquals(1, user.getMemberships().size());
        assertEquals("Mastercard", user.getMemberships().get(0).getName());
    }

    /**
     * Tests adding a membership to the user.
     */
    @Test
    void addMembershipAddsToCollection() {
        User user = new User();
        Membership membership = new Membership("Amex");

        user.addMembership(membership);

        assertEquals(1, user.getMemberships().size());
        assertTrue(user.getMemberships().contains(membership));
    }

    /**
     * Tests removing a membership from the user.
     */
    @Test
    void removeMembershipRemovesFromCollection() {
        User user = new User();
        Membership membership = new Membership("Amex");
        user.addMembership(membership);

        user.removeMembership(membership);

        assertEquals(0, user.getMemberships().size());
        assertFalse(user.getMemberships().contains(membership));
    }
}
