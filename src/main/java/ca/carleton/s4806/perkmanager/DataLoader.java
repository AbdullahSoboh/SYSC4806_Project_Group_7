package ca.carleton.s4806.perkmanager;

import ca.carleton.s4806.perkmanager.model.Membership;
import ca.carleton.s4806.perkmanager.model.Perk;
import ca.carleton.s4806.perkmanager.repository.MembershipRepository;
import ca.carleton.s4806.perkmanager.repository.PerkRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Data loader that pre-loads sample Memberships and Perks into the database
 * on application startup if the database is empty.
 * This ensures we always have demo data available, even after restarts.
 *
 * @author Moesa
 * @version 1.0
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final MembershipRepository membershipRepository;
    private final PerkRepository perkRepository;

    public DataLoader(MembershipRepository membershipRepository, PerkRepository perkRepository) {
        this.membershipRepository = membershipRepository;
        this.perkRepository = perkRepository;
    }

    /**
     * Runs on application startup and pre-loads data if database is empty.
     *
     * @param args command line arguments
     */
    @Override
    public void run(String... args) {
        // Check if database already has data
        if (membershipRepository.count() > 0) {
            System.out.println("Database already contains data. Skipping pre-load.");
            return;
        }

        System.out.println("Database is empty. Pre-loading sample data...");

        // Create and save Memberships
        Membership visa = membershipRepository.save(new Membership("Visa"));
        Membership mastercard = membershipRepository.save(new Membership("Mastercard"));
        Membership airMiles = membershipRepository.save(new Membership("Air Miles"));
        Membership caa = membershipRepository.save(new Membership("CAA"));
        Membership studentId = membershipRepository.save(new Membership("Student ID"));
        Membership costco = membershipRepository.save(new Membership("Costco"));
        Membership amazonPrime = membershipRepository.save(new Membership("Amazon Prime"));

        System.out.println("✓ Loaded 7 memberships");

        // Create and save sample Perks
        perkRepository.save(new Perk(
                "10% off Movie Tickets",
                "Get 10% discount on all movie tickets at Cineplex",
                "Movie Tickets",
                visa,
                LocalDate.of(2026, 12, 31),
                "Canada"
        ));

        perkRepository.save(new Perk(
                "Free Domestic Flight",
                "Redeem 10,000 miles for a free one-way domestic flight",
                "Flight",
                airMiles,
                LocalDate.of(2025, 12, 31),
                "Canada"
        ));

        perkRepository.save(new Perk(
                "Free Roadside Assistance",
                "24/7 roadside assistance including towing up to 200km",
                "Towing",
                caa,
                LocalDate.of(2026, 6, 30),
                "Ontario"
        ));

        perkRepository.save(new Perk(
                "Student Discount - Adobe Creative Cloud",
                "60% off Adobe Creative Cloud subscription",
                "Software",
                studentId,
                LocalDate.of(2025, 8, 31),
                "Global"
        ));

        perkRepository.save(new Perk(
                "$10 off Gas",
                "Save $10 on gas purchases over $50",
                "Gas",
                costco,
                LocalDate.of(2025, 11, 30),
                "Canada"
        ));

        perkRepository.save(new Perk(
                "Free Two-Day Shipping",
                "Free two-day shipping on all eligible items",
                "Shipping",
                amazonPrime,
                LocalDate.of(2026, 12, 31),
                "Canada"
        ));

        perkRepository.save(new Perk(
                "2% Cash Back on Groceries",
                "Earn 2% cash back on all grocery purchases",
                "Groceries",
                mastercard,
                LocalDate.of(2026, 3, 31),
                "Canada"
        ));

        perkRepository.save(new Perk(
                "Free Hotel Night",
                "Redeem 15,000 miles for a free hotel night",
                "Hotel",
                airMiles,
                LocalDate.of(2025, 12, 31),
                "Canada"
        ));

        perkRepository.save(new Perk(
                "20% off Restaurants",
                "Get 20% off at participating restaurants",
                "Dining",
                visa,
                LocalDate.of(2025, 11, 15),
                "Ottawa, ON"
        ));

        perkRepository.save(new Perk(
                "Student Transit Pass Discount",
                "Save 40% on monthly transit passes",
                "Transit",
                studentId,
                LocalDate.of(2026, 5, 31),
                "Ottawa, ON"
        ));

        perkRepository.save(new Perk(
                "Free Prime Video",
                "Access to unlimited streaming of movies and TV shows",
                "Streaming",
                amazonPrime,
                LocalDate.of(2026, 12, 31),
                "Global"
        ));

        perkRepository.save(new Perk(
                "CAA Travel Discounts",
                "Save up to 15% on hotels and car rentals",
                "Travel",
                caa,
                LocalDate.of(2026, 12, 31),
                "Global"
        ));

        System.out.println("✓ Loaded 12 sample perks");
        System.out.println("✓ Data pre-loading complete!");
    }
}