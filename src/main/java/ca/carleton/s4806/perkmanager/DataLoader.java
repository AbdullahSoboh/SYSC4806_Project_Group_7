package ca.carleton.s4806.perkmanager;

import ca.carleton.s4806.perkmanager.model.Membership;
import ca.carleton.s4806.perkmanager.model.Perk;
import ca.carleton.s4806.perkmanager.model.User;
import ca.carleton.s4806.perkmanager.repository.MembershipRepository;
import ca.carleton.s4806.perkmanager.repository.PerkRepository;
import ca.carleton.s4806.perkmanager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Data loader that pre-loads sample Memberships and Perks into the database
 * on application startup if the database is empty.
 * This ensures we always have demo data available, even after restarts.
 *
 * @author Moesa
 * @version 2.0
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final PerkRepository perkRepository;

    public DataLoader(UserRepository userRepository, MembershipRepository membershipRepository, PerkRepository perkRepository) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.perkRepository = perkRepository;
    }

    /**
     * Runs on application startup and pre-loads data if database is empty.
     *
     * @param args command line arguments
     */
    @Override
    public void run(String... args) throws Exception {
        // Create Memberships
        Membership visa = new Membership("Visa");
        Membership mastercard = new Membership("Mastercard");
        Membership caa = new Membership("CAA");
        Membership studentId = new Membership("StudentID");
        Membership costco = new Membership("Costco");

        membershipRepository.saveAll(Arrays.asList(visa, mastercard, caa, studentId, costco));

        // Create Users
        User admin = new User("admin", "password", "admin@example.com", Arrays.asList(visa, mastercard, caa, studentId, costco));
        User student = new User("student", "password", "student@example.com", Arrays.asList(studentId, visa));
        User parent = new User("parent", "password", "parent@example.com", Arrays.asList(costco, caa));

        userRepository.saveAll(Arrays.asList(admin, student, parent));

        // Create Perks
        createPerks(visa, mastercard, caa, studentId, costco);
    }

    private void createPerks(Membership visa, Membership mastercard, Membership caa, Membership studentId, Membership costco) {
        List<Perk> perks = Arrays.asList(
                new Perk("5% Cash Back", "Get 5% cash back on all grocery purchases.", "Groceries", visa, LocalDate.now().plusMonths(12), "Global"),
                new Perk("Free Travel Insurance", "Comprehensive travel insurance for trips up to 30 days.", "Travel", visa, LocalDate.now().plusMonths(12), "Global"),
                new Perk("Airport Lounge Access", "Access to over 1000 airport lounges worldwide.", "Travel", visa, LocalDate.now().plusMonths(12), "Global"),
                new Perk("Extended Warranty", "Double the manufacturer's warranty on eligible items.", "Electronics", visa, LocalDate.now().plusMonths(24), "Global"),

                new Perk("3% Gas Rebate", "3% rebate on gas station purchases.", "Gas", mastercard, LocalDate.now().plusMonths(12), "Global"),
                new Perk("Concert Presale", "Access to concert tickets before the general public.", "Entertainment", mastercard, LocalDate.now().plusMonths(6), "Global"),
                new Perk("Price Protection", "Refund of price difference if item price drops within 60 days.", "Shopping", mastercard, LocalDate.now().plusMonths(12), "Global"),
                new Perk("Mobile Device Insurance", "Coverage for lost or damaged mobile devices.", "Electronics", mastercard, LocalDate.now().plusMonths(12), "Global"),

                new Perk("Free Towing", "Free towing up to 200km.", "Auto", caa, LocalDate.now().plusMonths(12), "Canada"),
                new Perk("Hotel Discounts", "Up to 20% off at participating hotels.", "Travel", caa, LocalDate.now().plusMonths(12), "Global"),
                new Perk("Dining Deals", "10% off at partner restaurants.", "Dining", caa, LocalDate.now().plusMonths(12), "Canada"),
                new Perk("Movie Ticket Savings", "Discounted movie tickets at Cineplex.", "Entertainment", caa, LocalDate.now().plusMonths(12), "Canada"),

                new Perk("10% Student Discount", "10% off at participating retail stores.", "Shopping", studentId, LocalDate.now().plusMonths(12), "Canada"),
                new Perk("Cheap Transit Pass", "Reduced rate for monthly transit passes.", "Transit", studentId, LocalDate.now().plusMonths(4), "Ottawa, ON"),
                new Perk("Library Access", "Access to university library resources.", "Education", studentId, LocalDate.now().plusMonths(12), "Ottawa, ON"),
                new Perk("Software Licenses", "Free access to Office 365 and other software.", "Software", studentId, LocalDate.now().plusMonths(12), "Global"),

                new Perk("Bulk Savings", "Exclusive savings on bulk items.", "Groceries", costco, LocalDate.now().plusMonths(12), "Global"),
                new Perk("Gas Savings", "Cheaper gas prices at Costco gas stations.", "Gas", costco, LocalDate.now().plusMonths(12), "Global"),
                new Perk("Tire Services", "Free tire rotation and balancing.", "Auto", costco, LocalDate.now().plusMonths(12), "Global"),
                new Perk("Pharmacy Savings", "Lower dispensing fees at Costco pharmacy.", "Health", costco, LocalDate.now().plusMonths(12), "Global"),

                new Perk("Car Rental Discount", "15% off car rentals.", "Travel", visa, LocalDate.now().plusMonths(6), "Global")
        );

        perkRepository.saveAll(perks);
    }
}