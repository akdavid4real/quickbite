package com.debbiecyber.QuickBite.configurations;

import com.debbiecyber.QuickBite.entity.MenuItem;
import com.debbiecyber.QuickBite.entity.Restaurant;
import com.debbiecyber.QuickBite.entity.User;
import com.debbiecyber.QuickBite.enums.CuisineType;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.enums.AccountStatus;
import com.debbiecyber.QuickBite.enums.VerificationStatus;
import com.debbiecyber.QuickBite.repository.MenuItemRepository;
import com.debbiecyber.QuickBite.repository.RestaurantRepository;
import com.debbiecyber.QuickBite.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

    private static final String DEMO_PASSWORD = "QuickBite123!";
    private static final String OWNER_EMAIL = "owner@quickbite.local";

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DemoDataInitializer(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            MenuItemRepository menuItemRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate
    ) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User owner = ensureUser(
                "QuickBite Demo Owner",
                OWNER_EMAIL,
                "+2348000000001",
                UserRole.RESTAURANT_OWNER
        );
        ensureUser("QuickBite Demo Customer", "customer@quickbite.local", "+2348000000010", UserRole.CUSTOMER);
        ensureUser("QuickBite Demo Rider", "rider@quickbite.local", "+2348000000020", UserRole.RIDER);
        ensureUser("QuickBite Demo Admin", "admin@quickbite.local", "+2348000000030", UserRole.ADMIN);

        if (!restaurantRepository.existsById(1L)) {
            jdbcTemplate.update("""
                    INSERT INTO restaurants (
                        id, owner_id, name, description, cuisine_type, address,
                        latitude, longitude, phone_number, logourl, rating,
                        is_open, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """,
                    1L, owner.getId(), "Jollof & Co.",
                    "Firewood flavour, generous portions, familiar comfort.",
                    CuisineType.NIGERIAN.name(), "Lekki Phase 1, Lagos, Nigeria",
                    6.4474, 3.4723, "+2348000000002",
                    "/assets/menu/jollof-chicken.png", 4.6, true);
        }

        Restaurant restaurant = restaurantRepository.findById(1L).orElseThrow();
        restaurant.setOwner(owner);
        restaurant.setName("Jollof & Co.");
        restaurant.setDescription("Firewood flavour, generous portions, familiar comfort.");
        restaurant.setCuisineType(CuisineType.NIGERIAN);
        restaurant.setAddress("Lekki Phase 1, Lagos, Nigeria");
        restaurant.setLatitude(6.4474);
        restaurant.setLongitude(3.4723);
        restaurant.setPhoneNumber("+2348000000002");
        restaurant.setLogoURL("/assets/menu/jollof-chicken.png");
        restaurant.setRating(4.6);
        restaurant.setIsOpen(true);
        restaurant.setVerificationStatus(VerificationStatus.VERIFIED);
        restaurant = restaurantRepository.save(restaurant);

        upsertMenu(11L, restaurant, "Party Jollof & Chicken",
                "Smoky party jollof with two pieces of flame-grilled chicken.",
                "Popular", "4500.00", "/assets/menu/jollof-chicken.png");
        upsertMenu(12L, restaurant, "Nigerian Jollof Rice",
                "Deeply seasoned tomato rice with peppers and our house spice blend.",
                "Rice", "2800.00", "/assets/menu/jollof-rice.webp");
        upsertMenu(13L, restaurant, "Fried Rice & Chicken",
                "Nigerian fried rice with vegetables, prawns and seasoned chicken.",
                "Rice", "4200.00", "/assets/menu/fried-rice.png");
        upsertMenu(14L, restaurant, "Classic Beef Suya",
                "Smoky beef skewers with yaji, onions, tomato and cucumber.",
                "Grills", "3500.00", "/assets/menu/beef-suya.webp");
        upsertMenu(15L, restaurant, "Grilled Tilapia",
                "Whole grilled tilapia finished with a bright Nigerian pepper sauce.",
                "Grills", "8500.00", "/assets/menu/grilled-tilapia.png");
        upsertMenu(16L, restaurant, "Moi Moi",
                "Steamed bean pudding, soft, savoury and perfect beside rice.",
                "Sides", "1200.00", "/assets/menu/moi-moi.png");
        upsertMenu(17L, restaurant, "Goat Meat Pepper Soup",
                "Aromatic pepper soup with tender goat meat and warming spices.",
                "Soups", "4800.00", "/assets/menu/pepper-soup.png");
    }

    private User ensureUser(String name, String email, String phoneNumber, UserRole role) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .name(name)
                        .email(email)
                        .password(passwordEncoder.encode(DEMO_PASSWORD))
                        .phoneNumber(phoneNumber)
                        .address("Lekki Phase 1, Lagos, Nigeria")
                        .role(role)
                        .build()));
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setVerificationStatus(
                role == UserRole.RESTAURANT_OWNER || role == UserRole.RIDER
                        ? VerificationStatus.VERIFIED
                        : VerificationStatus.NOT_REQUIRED
        );
        return userRepository.save(user);
    }

    private void upsertMenu(
            Long id,
            Restaurant restaurant,
            String name,
            String description,
            String category,
            String price,
            String imageURL
    ) {
        if (!menuItemRepository.existsById(id)) {
            jdbcTemplate.update("""
                    INSERT INTO menu_items (
                        id, restaurant_id, name, description, category, price,
                        imageurl, is_available, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """,
                    id, restaurant.getId(), name, description, category, price, imageURL, true);
        }

        MenuItem item = menuItemRepository.findById(id).orElseThrow();
        item.setRestaurant(restaurant);
        item.setName(name);
        item.setDescription(description);
        item.setCategory(category);
        item.setPrice(new BigDecimal(price));
        item.setImageURL(imageURL);
        item.setIsAvailable(true);
        menuItemRepository.save(item);
    }
}
