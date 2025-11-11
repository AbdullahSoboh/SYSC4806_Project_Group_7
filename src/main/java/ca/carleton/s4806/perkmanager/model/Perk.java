package ca.carleton.s4806.perkmanager.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Represents a Perk entity in the Perk Manager application.
 * A perk is a discount or benefit associated with a specific membership and product.
 *
 * @author Moesa Malik
 * @version 2.0
 */
@Entity
public class Perk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for the perk

    private String title; // Title or name of the perk

    @Column(length = 1000)
    private String description; // Detailed description of the perk

    private String product; // The product this perk applies to (e.g., "Movie Tickets", "Flight")

    @ManyToOne
    @JoinColumn(name = "membership_id")
    private Membership membership; // The membership required for this perk (relationship to Membership entity)

    private Integer upvotes = 0; // Number of upvotes this perk has received from users

    private Integer downvotes = 0; // Number of downvotes this perk has received from users

    private String location; // Geographic location where the perk is valid (e.g., "Ottawa, ON")

    @Column(name = "expiry_date")
    private LocalDate expiryDate; // Expiry date of the perk

    /**
     * Default constructor required by JPA.
     */
    public Perk() {
    }

    /**
     * Constructs a new Perk with the specified details.
     *
     * @param title the title of the perk
     * @param description the description of the perk
     * @param product the product this perk applies to
     * @param membership the membership entity required for this perk
     * @param expiryDate the expiry date of the perk
     * @param location the geographic location where the perk is valid
     */
    public Perk(String title, String description, String product, Membership membership, LocalDate expiryDate, String location) {
        this.title = title;
        this.description = description;
        this.product = product;
        this.membership = membership;
        this.expiryDate = expiryDate;
        this.location = location;
    }

    /**
     * Gets the unique identifier of this perk.
     *
     * @return the perk ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this perk.
     *
     * @param id the perk ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the title of this perk.
     *
     * @return the perk title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of this perk.
     *
     * @param title the perk title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the description of this perk.
     *
     * @return the perk description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this perk.
     *
     * @param description the perk description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the product this perk applies to.
     *
     * @return the product name
     */
    public String getProduct() {
        return product;
    }

    /**
     * Sets the product this perk applies to.
     *
     * @param product the product name to set
     */
    public void setProduct(String product) {
        this.product = product;
    }

    /**
     * Gets the membership required for this perk.
     *
     * @return the membership entity
     */
    public Membership getMembership() {
        return membership;
    }

    /**
     * Sets the membership required for this perk.
     *
     * @param membership the membership entity to set
     */
    public void setMembership(Membership membership) {
        this.membership = membership;
    }

    /**
     * Gets the number of upvotes for this perk.
     *
     * @return the upvote count
     */
    public Integer getUpvotes() {
        return upvotes;
    }

    /**
     * Sets the number of upvotes for this perk.
     *
     * @param upvotes the upvote count to set
     */
    public void setUpvotes(Integer upvotes) {
        this.upvotes = upvotes;
    }

    /**
     * Gets the number of downvotes for this perk.
     *
     * @return the downvotes count
     */
    public Integer getDownvotes() {
        return downvotes;
    }

    /**
     * Sets the number of downvotes for this perk.
     *
     * @param downvotes the downvotes count to set
     */
    public void setDownvotes(Integer downvotes) {
        this.downvotes = downvotes;
    }

    /**
     * Gets the geographic location where this perk is valid.
     * This location typically represents a city or region (Ex. "Ottawa, ON").
     *
     * @return the location of the perk, or null if the perk is global
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the geographic location where this perk is valid.
     * This should be a city or region (Ex. "Ottawa, ON"). If null,
     * the perk may be considered applicable globally.
     *
     * @param location the location to set for this perk
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the expiry date of this perk.
     *
     * @return the expiry date
     */
    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets the expiry date of this perk.
     *
     * @param expiryDate the expiry date to set
     */
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * Gets the overall score of the perk (upvotes minus downvotes).
     * @return the score
     */
    public int getScore() {
        int up = (upvotes == null) ? 0 : upvotes;
        int down = (downvotes == null) ? 0 : downvotes;
        return up - down;
    }
}