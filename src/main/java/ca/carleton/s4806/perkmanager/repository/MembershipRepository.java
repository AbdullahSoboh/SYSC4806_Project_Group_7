package ca.carleton.s4806.perkmanager.repository;

import ca.carleton.s4806.perkmanager.model.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Membership entity.
 * Provides CRUD operations for Membership data.
 * Extends JpaRepository to inherit standard data access methods.
 * Inherited methods include:
 * - save(Membership membership) - Saves or updates a membership
 * - findById(Long id) - Finds a membership by its ID
 * - findAll() - Returns all memberships
 * - deleteById(Long id) - Deletes a membership by its ID
 * - count() - Returns the total number of memberships
 *
 * @author Moesa Malik
 * @version 1.0
 */
@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    // Custom query methods can be added here if needed, Ex.:
    // Optional<Membership> findByName(String name);
}