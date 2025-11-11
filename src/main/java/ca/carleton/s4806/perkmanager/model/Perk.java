package ca.carleton.s4806.perkmanager.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Represents a Perk entity in the Perk Manager application.
 * A perk is a discount or benefit associated with a specific membership and product.
 *
 * @author Moesa Malik, Imann Brar
 * @version 1.2
 */
@Entity
public class Perk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for the perk

    private String title; // Title or name of the perk

    @Column(length = 1000)
    private String description; // Detailed description of the perk

    private String product; // The product this perk applies to (Ex. "Movie Tickets", "Flight")

    private String membership; // The membership required for this perk (Ex. "Visa Card", "Air Miles")

    private Integer upvotes = 0; // Number of upvotes this perk has received from users

    private Integer downvotes = 0; // Number of downvotes this perk has received from users

    /**
     * Aggregated vote counter for simple +/- voting use-cases.
     * Initialized to 0 and persisted by JPA.
     * (This does NOT replace upvotes/downvotes; it’s an additional field.)
     */
    @Column(nullable = false)
    private int votes = 0;

    private String location;   // Geographic location where the perk is valid (Ex. "Ottawa, ON")

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
     * @param membership the membership required for this perk
     * @param expiryDate the expiry date of the perk
     * @param location the location where this perk is valid
     */
    public Perk(String title, String description, String product, String membership, LocalDate expiryDate, String location) {
        this.title = title;
        this.description = description;
        this.product = product;
        this.membership = membership;
        this.upvotes = 0;
        this.downvotes = 0;
        this.votes = 0;
        this.expiryDate = expiryDate;
        this.location = location;
    }

    /** Gets the unique identifier of this perk. */
    public Long getId() {
        return id;
    }

    /** Sets the unique identifier of this perk. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Gets the title of this perk. */
    public String getTitle() {
        return title;
    }

    /** Sets the title of this perk. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** Gets the description of this perk. */
    public String getDescription() {
        return description;
    }

    /** Sets the description of this perk. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Gets the product this perk applies to. */
    public String getProduct() {
        return product;
    }

    /** Sets the product this perk applies to. */
    public void setProduct(String product) {
        this.product = product;
    }

    /** Gets the membership required for this perk. */
    public String getMembership() {
        return membership;
    }

    /** Sets the membership required for this perk. */
    public void setMembership(String membership) {
        this.membership = membership;
    }

    /** Gets the number of upvotes for this perk. */
    public Integer getUpvotes() {
        return upvotes;
    }

    /** Sets the number of upvotes for this perk. */
    public void setUpvotes(Integer upvotes) {
        this.upvotes = upvotes;
    }

    /** Gets the number of downvotes for this perk. */
    public Integer getDownvotes() {
        return downvotes;
    }

    /** Sets the number of downvotes for this perk. */
    public void setDownvotes(Integer downvotes) {
        this.downvotes = downvotes;
    }

    /** Gets the aggregated votes counter. */
    public int getVotes() {
        return votes;
    }

    /** Sets the aggregated votes counter. */
    public void setVotes(int votes) {
        this.votes = votes;
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
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /** Gets the expiry date of this perk. */
    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    /** Sets the expiry date of this perk. */
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * Gets the overall score of the perk (upvotes minus downvotes).
     * (Unchanged behavior; independent of the new `votes` field.)
     */
    public int getScore() {
        int up = (upvotes == null) ? 0 : upvotes;
        int down = (downvotes == null) ? 0 : downvotes;
        return up - down;
    }
}
