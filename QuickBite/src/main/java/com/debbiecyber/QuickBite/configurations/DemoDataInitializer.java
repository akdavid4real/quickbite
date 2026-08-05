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

        Restaurant restaurant = ensureRestaurant(
                1L, owner, "Jollof & Co.",
                "Firewood flavour, generous portions, familiar comfort.",
                CuisineType.NIGERIAN, "Lekki Phase 1, Lagos, Nigeria",
                6.4474, 3.4723, "+2348000000002",
                "/assets/menu/jollof-chicken.png", 4.6
        );

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
        upsertMenu(18L, restaurant, "Coconut Rice & Prawns",
                "Fragrant coconut rice tossed with vegetables and juicy prawns.",
                "Rice", "4600.00", "/assets/menu/coconut-rice.webp");
        upsertMenu(19L, restaurant, "Ofada Rice & Ayamase",
                "Local Ofada rice served with bold green pepper ayamase and egg.",
                "Rice", "5000.00", "/assets/menu/ofada-rice-ayamase.webp");
        upsertMenu(101L, restaurant, "Asaro Yam Porridge",
                "Soft yam simmered in a rich palm-oil pepper sauce.",
                "Mains", "3400.00", "/assets/menu/asaro-yam-porridge.webp");
        upsertMenu(102L, restaurant, "Nigerian Meat Pie",
                "Golden pastry filled with seasoned minced beef and vegetables.",
                "Pastries", "1600.00", "/assets/menu/meat-pie.webp");
        upsertMenu(103L, restaurant, "Abacha African Salad",
                "Eastern Nigerian cassava salad with fish, vegetables and ugba.",
                "Mains", "4200.00", "/assets/menu/abacha.webp");

        Restaurant mamaTs = ensureRestaurant(
                2L, owner, "Mama T's Kitchen",
                "Rich soups and proper home-style classics.",
                CuisineType.NIGERIAN, "Yaba, Lagos, Nigeria",
                6.5158, 3.3899, "+2348000000003",
                "/assets/efo-riro.png", 4.5
        );
        upsertMenu(21L, mamaTs, "Efo Riro & Pounded Yam",
                "Deeply seasoned vegetable stew with assorted meat and smooth pounded yam.",
                "Swallow", "5200.00", "/assets/efo-riro.png");
        upsertMenu(22L, mamaTs, "Egusi Soup & Pounded Yam",
                "Melon seed soup loaded with greens and assorted meat, served with pounded yam.",
                "Swallow", "5400.00", "/assets/menu/egusi-soup.webp");
        upsertMenu(23L, mamaTs, "Beans Porridge & Plantain",
                "Creamy Nigerian beans porridge served with sweet fried plantain.",
                "Mains", "3200.00", "/assets/menu/beans-porridge.webp");
        upsertMenu(24L, mamaTs, "White Rice & Tomato Stew",
                "Steamed rice with slow-cooked Nigerian tomato stew and tender beef.",
                "Rice", "3800.00", "/assets/menu/tomato-stew.webp");
        upsertMenu(25L, mamaTs, "Moi Moi",
                "Steamed bean pudding with peppers and traditional spices.",
                "Sides", "1200.00", "/assets/menu/moi-moi.png");
        upsertMenu(26L, mamaTs, "Akara Breakfast",
                "Crisp bean fritters served hot for a simple Lagos breakfast.",
                "Breakfast", "1800.00", "/assets/menu/akara.webp");
        upsertMenu(27L, mamaTs, "Ogbono Soup & Eba",
                "Draw soup made with ground ogbono, greens and assorted meat.",
                "Swallow", "5200.00", "/assets/menu/ogbono-soup.webp");
        upsertMenu(28L, mamaTs, "Okra Soup & Pounded Yam",
                "Fresh chopped okra cooked with fish and assorted meat.",
                "Swallow", "5300.00", "/assets/menu/okra-soup.webp");
        upsertMenu(29L, mamaTs, "Afang Soup & Fufu",
                "A hearty Cross River vegetable soup with assorted meat.",
                "Swallow", "5600.00", "/assets/menu/afang-soup.webp");
        upsertMenu(30L, mamaTs, "Banga Soup & Starch",
                "Palm fruit soup with traditional spices, fish and meat.",
                "Swallow", "5700.00", "/assets/menu/banga-soup.webp");
        upsertMenu(201L, mamaTs, "Pap & Akara",
                "Creamy pap served with freshly fried bean cakes.",
                "Breakfast", "2300.00", "/assets/menu/pap-and-akara.webp");

        Restaurant suyaRepublic = ensureRestaurant(
                3L, owner, "Suya Republic",
                "Smoky skewers, bold yaji and late-night favourites.",
                CuisineType.FAST_FOOD, "Surulere, Lagos, Nigeria",
                6.5059, 3.3509, "+2348000000004",
                "/assets/suya.png", 4.7
        );
        upsertMenu(31L, suyaRepublic, "Classic Beef Suya",
                "Smoky beef skewers with onions, cabbage and yaji.",
                "Grills", "3500.00", "/assets/menu/beef-suya.webp");
        upsertMenu(32L, suyaRepublic, "Chicken Suya",
                "Juicy grilled chicken coated in a bold house yaji blend.",
                "Grills", "4000.00", "/assets/menu/chicken-suya.webp");
        upsertMenu(33L, suyaRepublic, "Grilled Tilapia",
                "Whole tilapia grilled over flame and finished with pepper sauce.",
                "Grills", "8500.00", "/assets/menu/grilled-tilapia.png");
        upsertMenu(34L, suyaRepublic, "Suya Party Box",
                "A generous sharing box of beef suya, vegetables and extra spice.",
                "Popular", "7500.00", "/assets/suya.png");
        upsertMenu(35L, suyaRepublic, "Goat Meat Pepper Soup",
                "Tender goat meat in a warming, aromatic pepper broth.",
                "Soups", "4800.00", "/assets/menu/pepper-soup.png");
        upsertMenu(36L, suyaRepublic, "Party Jollof & Suya",
                "Smoky party jollof paired with freshly grilled beef suya.",
                "Rice", "5200.00", "/assets/menu/jollof-rice.webp");
        upsertMenu(37L, suyaRepublic, "Nkwobi",
                "Tender cow foot tossed in a spicy palm-oil sauce.",
                "Native Pots", "5200.00", "/assets/menu/nkwobi.webp");
        upsertMenu(38L, suyaRepublic, "Isi Ewu",
                "Traditional spiced goat head finished with onions and herbs.",
                "Native Pots", "6500.00", "/assets/menu/isi-ewu.webp");
        upsertMenu(39L, suyaRepublic, "Puff Puff",
                "Soft, golden Nigerian dough bites served warm.",
                "Small Chops", "1500.00", "/assets/menu/puff-puff.webp");
        upsertMenu(40L, suyaRepublic, "Bole & Grilled Fish",
                "Roasted plantain with grilled fish and fiery pepper sauce.",
                "Grills", "6800.00", "/assets/menu/bole-grilled-fish.webp");
        upsertMenu(41L, suyaRepublic, "Asun",
                "Smoky grilled goat meat tossed with chopped peppers and onions.",
                "Grills", "5000.00", "/assets/menu/asun.webp");
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

    private Restaurant ensureRestaurant(
            Long id,
            User owner,
            String name,
            String description,
            CuisineType cuisineType,
            String address,
            Double latitude,
            Double longitude,
            String phoneNumber,
            String logoURL,
            Double rating
    ) {
        if (!restaurantRepository.existsById(id)) {
            jdbcTemplate.update("""
                    INSERT INTO restaurants (
                        id, owner_id, name, description, cuisine_type, address,
                        latitude, longitude, phone_number, logourl, rating,
                        is_open, verification_status, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """,
                    id, owner.getId(), name, description, cuisineType.name(), address,
                    latitude, longitude, phoneNumber, logoURL, rating, true,
                    VerificationStatus.VERIFIED.name());
        }

        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow();
        restaurant.setOwner(owner);
        restaurant.setName(name);
        restaurant.setDescription(description);
        restaurant.setCuisineType(cuisineType);
        restaurant.setAddress(address);
        restaurant.setLatitude(latitude);
        restaurant.setLongitude(longitude);
        restaurant.setPhoneNumber(phoneNumber);
        restaurant.setLogoURL(logoURL);
        restaurant.setRating(rating);
        restaurant.setIsOpen(true);
        restaurant.setVerificationStatus(VerificationStatus.VERIFIED);
        return restaurantRepository.save(restaurant);
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
