package ca.carleton.s4806.perkmanager.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Represents a Perk entity in the Perk Manager application.
 * A perk is a discount or benefit associated with a specific membership and product.
 *
 * @author Moesa Malik
 * @version 1.0
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

    private Integer upvotes; // Number of upvotes this perk has received from users

    @Column(name = "expiry_date")
    private LocalDate expiryDate; // Expiry date of the perk

    /**
     * Default constructor required by JPA.
     * Initializes upvotes to 0.
     */
    public Perk() {
        this.upvotes = 0;
    }

    /**
     * Constructs a new Perk with the specified details.
     *
     * @param title the title of the perk
     * @param description the description of the perk
     * @param product the product this perk applies to
     * @param membership the membership required for this perk
     * @param expiryDate the expiry date of the perk
     */
    public Perk(String title, String description, String product, String membership, LocalDate expiryDate) {
        this.title = title;
        this.description = description;
        this.product = product;
        this.membership = membership;
        this.upvotes = 0;
        this.expiryDate = expiryDate;
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
     * @return the membership name
     */
    public String getMembership() {
        return membership;
    }

    /**
     * Sets the membership required for this perk.
     *
     * @param membership the membership name to set
     */
    public void setMembership(String membership) {
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
}