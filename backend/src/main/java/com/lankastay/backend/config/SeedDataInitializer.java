package com.lankastay.backend.config;

import com.lankastay.backend.repository.DestinationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class SeedDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SeedDataInitializer.class);

    private final DestinationRepository destinationRepository;
    private final JdbcTemplate jdbcTemplate;

    public SeedDataInitializer(DestinationRepository destinationRepository, JdbcTemplate jdbcTemplate) {
        this.destinationRepository = destinationRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Domain seed data is intentionally separate from authentication bootstrap.
        try {
            seedRoomsAndRatesAndReservations(LocalDateTime.now());
        } catch (Exception ex) {
            logger.warn("Could not seed domain entities: {}", ex.getMessage());
        }

        long count = destinationRepository.count();
        if (count > 0) {
            logger.info("Database already contains {} destination records. Skipping seed data initialization.", count);
            return;
        }

        logger.info("Initializing 9 canonical LankaStay seed destinations into MySQL database...");

        LocalDateTime now = LocalDateTime.now();

        // 1. Colombo
        insertDestination(1L, "Colombo", "colombo",
                "Discover Sri Lanka's vibrant capital with modern city stays, dining, shopping, and coastal attractions.",
                "Explore Sri Lanka's vibrant capital, offering modern hotel stays, shopping precincts, colonial architecture, and seaside dining along Galle Face Green.",
                "Urban Escape", "Western Province", "Colombo", 6.9271, 79.8612, "ACTIVE", "/assets/images/colombo.png", now);

        // 2. Negombo
        insertDestination(2L, "Negombo", "negombo",
                "Relax along Sri Lanka's western coast with beaches, lagoon views, seafood, and easy access to the airport.",
                "Negombo is a bustling coastal resort town featuring wide golden sandy beaches, colorful fishing lagoons, colonial Dutch canals, and fresh seafood dining.",
                "Coastal Escape", "Western Province", "Gampaha", 7.2083, 79.8358, "ACTIVE", "/assets/images/negombo.png", now);

        // 3. Galle
        insertDestination(3L, "Galle", "galle",
                "Explore historic Galle Fort, southern beaches, coastal resorts, and Sri Lanka's rich colonial heritage.",
                "Galle is famous for its UNESCO World Heritage Dutch Fort, historic ramparts, cobblestone streets, boutique stays, and spectacular southern beaches.",
                "Heritage & Coast", "Southern Province", "Galle", 6.0329, 80.2168, "ACTIVE", "/assets/images/galle.png", now);

        // 4. Sigiriya
        insertDestination(4L, "Sigiriya", "sigiriya",
                "Stay close to the iconic Sigiriya Rock Fortress while exploring culture, wildlife, and peaceful natural surroundings.",
                "Located in the Cultural Triangle, Sigiriya features the ancient 5th-century rock citadel, ancient frescoes, water gardens, and rich surrounding wilderness.",
                "Heritage & Nature", "Central Province", "Matale", 7.9570, 80.7603, "ACTIVE", "/assets/images/sigiriya.png", now);

        // 5. Nuwara Eliya
        insertDestination(5L, "Nuwara Eliya", "nuwara-eliya",
                "Experience cool mountain weather, tea estates, gardens, and relaxing hill-country stays.",
                "Nuwara Eliya is Sri Lanka's cool-climate hill resort surrounded by verdant tea plantations, colonial bungalows, waterfalls, and scenic mountain ranges.",
                "Hill Country", "Central Province", "Nuwara Eliya", 6.9497, 80.7891, "ACTIVE", "/assets/images/nuwara-eliya.png", now);

        // 6. Yala
        insertDestination(6L, "Yala", "yala",
                "Discover safari experiences, wildlife, nature lodges, and unforgettable stays near Yala National Park.",
                "Yala is renowned worldwide for safari adventures, offering one of the highest leopard densities in the world alongside elephants, sloth bears, and coastal lagoons.",
                "Wildlife & Nature", "Southern Province", "Hambantota", 6.3725, 81.5185, "ACTIVE", "/assets/images/yala.png", now);

        // 7. Kandy
        insertDestination(7L, "Kandy", "kandy",
                "Explore Sri Lanka's cultural capital with temples, lakeside views, hill-country scenery, and heritage experiences.",
                "Nestled among green hills, Kandy is home to the sacred Temple of the Tooth Relic, scenic Kandy Lake, botanical gardens, and vibrant traditional dance.",
                "Culture & Hills", "Central Province", "Kandy", 7.2906, 80.6337, "ACTIVE", "/assets/images/home/hero/kandy-lake-resort.png", now);

        // 8. Ella
        insertDestination(8L, "Ella", "ella",
                "Enjoy scenic mountain views, waterfalls, hiking trails, and peaceful stays in one of Sri Lanka's favourite hill towns.",
                "Ella is a tranquil mountain village famous for Nine Arches Bridge, Little Adam's Peak hikes, sweeping valleys, tea fields, and panoramic mountain views.",
                "Mountain Escape", "Uva Province", "Badulla", 6.8667, 81.0466, "ACTIVE", "/assets/images/nuwara-eliya.png", now);

        // 9. Trincomalee
        insertDestination(9L, "Trincomalee", "trincomalee",
                "Discover pristine natural harbors, golden beaches, hot springs, and sacred historic temples in Eastern Sri Lanka.",
                "Situated on Sri Lanka's eastern coast, Trincomalee features one of the finest natural deep-water harbors, Koneswaram Hindu Temple, Pigeon Island marine park, and clear blue ocean waters.",
                "Heritage & Coast", "Eastern Province", "Trincomalee", 8.5874, 81.2152, "ACTIVE", "/assets/images/galle.png", now);

        // 4 Galle Attractions
        insertAttraction(1L, 3L, "Galle Fort", "HERITAGE", "The UNESCO-listed fortified old town at the heart of historic Galle.", 6.0305, 80.2167, "ACTIVE", 0, now);
        insertAttraction(2L, 3L, "Galle Lighthouse", "HERITAGE", "A landmark lighthouse on the southern edge of Galle Fort.", 6.0247, 80.2197, "ACTIVE", 1, now);
        insertAttraction(3L, 3L, "Unawatuna Beach", "BEACH", "A popular sheltered bay south of Galle.", 6.0097, 80.2494, "ACTIVE", 2, now);
        insertAttraction(4L, 3L, "Japanese Peace Pagoda", "RELIGIOUS", "A hilltop pagoda overlooking the coast.", 6.0187, 80.2394, "ACTIVE", 3, now);

        logger.info("Successfully seeded 9 canonical destinations into MySQL database via JdbcTemplate.");

        seedHotelsIfEmpty(now);
    }

    private void seedHotelsIfEmpty(LocalDateTime now) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hotels", Integer.class);
        if (count != null && count > 0) {
            logger.info("Database already contains {} hotel records. Skipping hotel seed.", count);
            return;
        }

        logger.info("Seeding canonical LankaStay hotels into MySQL...");
        // 101 LankaStay City Grand
        insertHotel(101L, "LankaStay City Grand", "lankastay-city-grand", 1L, "Hotel", "City Hotel", 4.7, 188, 32000.0, "/assets/images/colombo.png", "Popular", true, "ACTIVE", "COMPLETE", "ACTIVE", "A modern luxury city stay with rooftop views and easy access to Colombo attractions.", now);
        // 102 LankaStay Ocean View
        insertHotel(102L, "LankaStay Ocean View", "lankastay-ocean-view", 1L, "Hotel", "Ocean View Hotel", 4.6, 152, 35000.0, "/assets/images/colombo.png", "Best View", false, "ACTIVE", "COMPLETE", "ACTIVE", "A stylish Colombo stay offering beautiful Indian Ocean views and modern comfort.", now);
        // 201 LankaStay Lagoon Resort
        insertHotel(201L, "LankaStay Lagoon Resort", "lankastay-lagoon-resort", 2L, "Resort", "Lagoon Resort", 4.7, 176, 38000.0, "/assets/images/negombo.png", "Best Value", true, "ACTIVE", "COMPLETE", "ACTIVE", "A peaceful tropical resort overlooking the lagoon with relaxing poolside spaces.", now);
        // 301 LankaStay Heritage Fort
        insertHotel(301L, "LankaStay Heritage Fort", "lankastay-heritage-fort", 3L, "Hotel", "Heritage Hotel", 4.8, 214, 62000.0, "/assets/images/galle.png", "Heritage", true, "ACTIVE", "COMPLETE", "ACTIVE", "A charming heritage stay inspired by colonial architecture near historic Galle Fort.", now);
        // 302 LankaStay Ocean Bay
        insertHotel(302L, "LankaStay Ocean Bay", "lankastay-ocean-bay", 3L, "Resort", "Coastal Resort", 4.9, 324, 48000.0, "/assets/images/galle.png", "Popular", true, "ACTIVE", "COMPLETE", "ACTIVE", "Beachfront luxury with breathtaking ocean views.", now);
        // 401 LankaStay Sigiriya Retreat
        insertHotel(401L, "LankaStay Sigiriya Retreat", "lankastay-sigiriya-retreat", 4L, "Resort", "Nature Resort", 4.9, 198, 52000.0, "/assets/images/sigiriya.png", "Top Rated", true, "ACTIVE", "COMPLETE", "ACTIVE", "A luxury nature retreat surrounded by greenery with impressive views toward Sigiriya.", now);
        // 501 LankaStay Highland Mist
        insertHotel(501L, "LankaStay Highland Mist", "lankastay-highland-mist", 5L, "Hotel", "Hill Country Hotel", 4.7, 165, 45000.0, "/assets/images/nuwara-eliya.png", "Hill Country", true, "ACTIVE", "COMPLETE", "ACTIVE", "A cosy hill-country hotel surrounded by cool mist, mountains, and tea estates.", now);
        // 601 LankaStay Yala Wild Retreat
        insertHotel(601L, "LankaStay Yala Wild Retreat", "lankastay-yala-wild-retreat", 6L, "Resort", "Wildlife Resort", 4.8, 221, 56000.0, "/assets/images/yala.png", "Safari Favourite", true, "ACTIVE", "COMPLETE", "ACTIVE", "A nature-focused retreat close to Yala with relaxing accommodation and safari experiences.", now);

        logger.info("Successfully seeded canonical hotels into MySQL.");
    }

    private void insertHotel(
            Long id, String name, String slug, Long destinationId, String propertyType, String category,
            double rating, int reviewCount, double price, String mainImage, String badge, boolean featured,
            String status, String setupStatus, String publicationStatus, String shortDescription, LocalDateTime now
    ) {
        String sql = "INSERT INTO hotels (id, name, slug, destination_id, property_type, category, rating, review_count, price, main_image, badge, featured, status, setup_status, publication_status, short_description, last_updated_section, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'basic', ?, ?) " +
                "ON DUPLICATE KEY UPDATE name=VALUES(name)";
        jdbcTemplate.update(sql, id, name, slug, destinationId, propertyType, category, rating, reviewCount, price, mainImage, badge, featured, status, setupStatus, publicationStatus, shortDescription, now, now);
    }

    private void insertDestination(
            Long id, String name, String slug, String shortDescription, String fullDescription,
            String category, String region, String district, double latitude, double longitude,
            String status, String mainImage, LocalDateTime now
    ) {
        String sql = "INSERT INTO destinations (id, name, slug, short_description, full_description, category, region, district, latitude, longitude, status, main_image, card_image_position, hero_image_position, hero_fit_mode, last_saved_step, last_completed_step, last_updated_section, version, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'center center', 'center center', 'cover', 6, 6, 'Initial Seed', 0, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name=VALUES(name)";
        jdbcTemplate.update(sql, id, name, slug, shortDescription, fullDescription, category, region, district, latitude, longitude, status, mainImage, now, now);
    }

    private void insertAttraction(
            Long id, Long destinationId, String name, String type, String shortDescription,
            double latitude, double longitude, String status, int displayOrder, LocalDateTime now
    ) {
        String sql = "INSERT INTO attractions (id, destination_id, name, type, short_description, latitude, longitude, status, display_order, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name=VALUES(name)";
        jdbcTemplate.update(sql, id, destinationId, name, type, shortDescription, latitude, longitude, status, displayOrder, now, now);
    }

    private void seedRoomsAndRatesAndReservations(LocalDateTime now) {
        try {
            Integer roomCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM rooms", Integer.class);
            if (roomCount == null || roomCount == 0) {
                jdbcTemplate.update("INSERT INTO rooms (id, hotel_id, name, slug, room_category, description, max_occupancy, max_adults, max_children, size_sqm, bed_type, inventory_count, base_price, status, main_image, amenities_json, created_at, updated_at) " +
                        "VALUES (101, 301, 'Deluxe Ocean View Suite', 'deluxe-ocean-view-suite', 'SUITE', 'Spacious oceanfront suite with private balcony and premium king bed.', 3, 2, 1, 48.0, 'King', 8, 38500.0, 'ACTIVE', '/assets/images/deluxe-ocean-view-room.png', '[\"Ocean View\",\"Private Balcony\",\"King Bed\",\"Wi-Fi\",\"Air Conditioning\"]', ?, ?)", now, now);

                jdbcTemplate.update("INSERT INTO rooms (id, hotel_id, name, slug, room_category, description, max_occupancy, max_adults, max_children, size_sqm, bed_type, inventory_count, base_price, status, main_image, amenities_json, created_at, updated_at) " +
                        "VALUES (102, 301, 'Premium Ocean Suite', 'premium-ocean-suite', 'SUITE', 'Luxury ocean suite with panoramic Indian Ocean views and soaking tub.', 4, 3, 2, 65.0, 'King + Sofa Bed', 5, 52000.0, 'ACTIVE', '/assets/images/premium-ocean-suite.png', '[\"Ocean View\",\"Jacuzzi\",\"King Bed\",\"Mini Bar\",\"Living Area\"]', ?, ?)", now, now);

                jdbcTemplate.update("INSERT INTO rooms (id, hotel_id, name, slug, room_category, description, max_occupancy, max_adults, max_children, size_sqm, bed_type, inventory_count, base_price, status, main_image, amenities_json, created_at, updated_at) " +
                        "VALUES (103, 302, 'Family Ocean Villa', 'family-ocean-villa', 'VILLA', 'Secluded private heritage villa with garden, private pool, and butler service.', 6, 4, 3, 110.0, '2 King Beds', 3, 89000.0, 'ACTIVE', '/assets/images/family-ocean-room.png', '[\"Private Pool\",\"Butler Service\",\"Plunge Pool\",\"Kitchenette\",\"Private Garden\"]', ?, ?)", now, now);

                logger.info("Seeded canonical Room records into MySQL database.");
            }

            Integer rateCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM room_rates", Integer.class);
            if (rateCount == null || rateCount == 0) {
                jdbcTemplate.update("INSERT INTO room_rates (id, hotel_id, room_id, rate_plan_name, rate_plan_code, base_nightly_rate, weekend_nightly_rate, meal_plan, cancellation_policy, deposit_required, deposit_percentage, status, created_at, updated_at) " +
                        "VALUES (1, 301, 101, 'Standard Flexible Rate', 'FLEX-BB', 38500.0, 42000.0, 'BED_AND_BREAKFAST', 'FLEXIBLE_24H', 0, 0.0, 'ACTIVE', ?, ?)", now, now);

                jdbcTemplate.update("INSERT INTO room_rates (id, hotel_id, room_id, rate_plan_name, rate_plan_code, base_nightly_rate, weekend_nightly_rate, meal_plan, cancellation_policy, deposit_required, deposit_percentage, status, created_at, updated_at) " +
                        "VALUES (2, 301, 102, 'Non-Refundable Saver', 'NR-RO', 46800.0, 50000.0, 'ROOM_ONLY', 'NON_REFUNDABLE', 1, 100.0, 'ACTIVE', ?, ?)", now, now);

                jdbcTemplate.update("INSERT INTO room_rates (id, hotel_id, room_id, rate_plan_name, rate_plan_code, base_nightly_rate, weekend_nightly_rate, meal_plan, cancellation_policy, deposit_required, deposit_percentage, status, created_at, updated_at) " +
                        "VALUES (3, 302, 103, 'Luxury Half Board Escape', 'LUX-HB', 89000.0, 95000.0, 'HALF_BOARD', 'FLEXIBLE_7D', 1, 50.0, 'ACTIVE', ?, ?)", now, now);

                logger.info("Seeded canonical RoomRate records into MySQL database.");
            }

            Integer resCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", Integer.class);
            if (resCount == null || resCount == 0) {
                java.time.LocalDate today = java.time.LocalDate.now();

                jdbcTemplate.update("INSERT INTO reservations (id, reservation_code, hotel_id, customer_id, guest_name, guest_email, guest_phone, check_in, check_out, number_of_nights, adults, children, total_amount, tax_amount, net_amount, payment_status, reservation_status, assignment_state, assigned_room_number, special_requests, estimated_arrival_time, created_at, updated_at) " +
                        "VALUES (1, 'LK-2026-8941', 301, 1, 'Kasun Perera', 'kasun.p@gmail.com', '+94 77 123 4567', ?, ?, 2, 2, 0, 77000.0, 7000.0, 70000.0, 'PAID', 'CONFIRMED', 'ASSIGNED', '204', 'High floor requested.', '14:30', ?, ?)", today, today.plusDays(2), now, now);

                jdbcTemplate.update("INSERT INTO reservations (id, reservation_code, hotel_id, customer_id, guest_name, guest_email, guest_phone, check_in, check_out, number_of_nights, adults, children, total_amount, tax_amount, net_amount, payment_status, reservation_status, assignment_state, assigned_room_number, special_requests, estimated_arrival_time, created_at, updated_at) " +
                        "VALUES (2, 'LK-2026-8942', 301, 2, 'Nimali Fernando', 'nimali.f@gmail.com', '+94 71 987 6543', ?, ?, 3, 2, 1, 156000.0, 14000.0, 142000.0, 'PARTIALLY_PAID', 'CONFIRMED', 'UNASSIGNED', NULL, 'Honeymoon arrangement.', '15:00', ?, ?)", today.plusDays(1), today.plusDays(4), now, now);

                jdbcTemplate.update("INSERT INTO reservation_items (id, reservation_id, room_id, room_rate_id, quantity, nightly_rate, total_price) " +
                        "VALUES (1, 1, 101, 1, 1, 38500.0, 77000.0)");

                jdbcTemplate.update("INSERT INTO reservation_items (id, reservation_id, room_id, room_rate_id, quantity, nightly_rate, total_price) " +
                        "VALUES (2, 2, 102, 2, 1, 52000.0, 156000.0)");

                logger.info("Seeded canonical Reservation records into MySQL database.");
            }
        } catch (Exception ex) {
            logger.warn("Could not seed rooms/rates/reservations: {}", ex.getMessage());
        }
    }
}
