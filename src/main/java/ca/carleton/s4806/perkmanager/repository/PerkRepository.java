package ca.carleton.s4806.perkmanager.repository;

import ca.carleton.s4806.perkmanager.model.Perk;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Perk entity.
 * Provides CRUD operations and custom query methods for Perk data.
 * Extends JpaRepository to inherit standard data access methods.
 * Inherited methods include:
 * - save(Perk perk) - Saves or updates a perk
 * - findById(Long id) - Finds a perk by its ID
 * - findAll() - Returns all perks
 * - deleteById(Long id) - Deletes a perk by its ID
 * - count() - Returns the total number of perks
 * - existsById(Long id) - Checks if a perk exists by ID
 *
 * @author Moesa Malik
 * @version 1.0
 */
@Repository
public interface PerkRepository extends JpaRepository<Perk, Long> {
    List<Perk> findByTitleContainingIgnoreCase(String titleKeyword);

    List<Perk> findByProductContainingIgnoreCase(String productKeyword);

    List<Perk> findByTitleContainingIgnoreCaseOrProductContainingIgnoreCase(
            String titleKeyword,
            String productKeyword
    );

    List<Perk> findByTitleContainingIgnoreCaseOrProductContainingIgnoreCase(
            String titleKeyword,
            String productKeyword,
            Sort sort
    );
}
