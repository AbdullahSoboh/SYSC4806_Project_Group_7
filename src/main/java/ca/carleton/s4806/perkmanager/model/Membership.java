package ca.carleton.s4806.perkmanager.model;

import jakarta.persistence.*;

/**
 * Represents a Membership entity in the Perk Manager application.
 * A membership is a card or program that provides access to perks
 * (Ex. Visa, Air Miles, Student ID).
 *
 * @author Moesa Malik
 * @version 1.0
 */
@Entity
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for the membership

    private String name; // Name of the membership (Ex. "Visa", "Air Miles")

    /**
     * Default constructor required by JPA.
     */
    public Membership() {
    }

    /**
     * Constructs a new Membership with the specified name.
     *
     * @param name the name of the membership
     */
    public Membership(String name) {
        this.name = name;
    }

    /**
     * Gets the unique identifier of this membership.
     *
     * @return the membership ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this membership.
     *
     * @param id the membership ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the name of this membership.
     *
     * @return the membership name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this membership.
     *
     * @param name the membership name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}