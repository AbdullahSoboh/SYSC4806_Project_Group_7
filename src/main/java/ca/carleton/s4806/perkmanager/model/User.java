package ca.carleton.s4806.perkmanager.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a User entity in the Perk Manager application.
 * A user can have multiple memberships.
 *
 * @author Moesa Malik
 * @version 1.0
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_memberships",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "membership_id")
    )
    private List<Membership> memberships = new ArrayList<>();

    /**
     * Default constructor required by JPA.
     */
    public User() {
    }

    /**
     * Constructs a new User with the specified details.
     *
     * @param username the username
     * @param password the password
     * @param email the email
     * @param memberships the list of memberships
     */
    public User(String username, String password, String email, List<Membership> memberships) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.memberships = memberships;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Membership> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<Membership> memberships) {
        this.memberships = memberships;
    }

    public void addMembership(Membership membership) {
        this.memberships.add(membership);
    }

    public void removeMembership(Membership membership) {
        this.memberships.remove(membership);
    }
}
