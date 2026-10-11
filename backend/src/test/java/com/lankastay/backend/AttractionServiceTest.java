package com.lankastay.backend;

import com.lankastay.backend.dto.attraction.AttractionCreateRequest;
import com.lankastay.backend.dto.attraction.AttractionResponse;
import com.lankastay.backend.dto.attraction.AttractionUpdateRequest;
import com.lankastay.backend.entity.Attraction;
import com.lankastay.backend.entity.AttractionStatus;
import com.lankastay.backend.entity.Destination;
import com.lankastay.backend.entity.DestinationStatus;
import com.lankastay.backend.exception.ResourceNotFoundException;
import com.lankastay.backend.repository.AttractionRepository;
import com.lankastay.backend.repository.DestinationRepository;
import com.lankastay.backend.service.AttractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AttractionServiceTest {

    @Autowired
    private AttractionService attractionService;

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private AttractionRepository attractionRepository;

    private Long colomboId;
    private Long galleId;

    @BeforeEach
    void setUp() {
        colomboId = createDestination("Attraction Test Colombo", "attraction-test-colombo", "Western Province", "Colombo", 6.9271, 79.8612).getId();
        Destination galle = createDestination("Attraction Test Galle", "attraction-test-galle", "Southern Province", "Galle", 6.0329, 80.2168);
        galleId = galle.getId();

        createAttraction(galle, "Galle Fort", "HERITAGE", 0);
        createAttraction(galle, "Galle Lighthouse", "HERITAGE", 1);
        createAttraction(galle, "Unawatuna Beach", "BEACH", 2);
        createAttraction(galle, "Japanese Peace Pagoda", "RELIGIOUS", 3);
    }

    private Destination createDestination(String name, String slug, String region, String district, double latitude, double longitude) {
        Destination destination = new Destination();
        destination.setName(name);
        destination.setSlug(slug);
        destination.setShortDescription("Attraction service test destination");
        destination.setRegion(region);
        destination.setDistrict(district);
        destination.setLatitude(latitude);
        destination.setLongitude(longitude);
        destination.setStatus(DestinationStatus.ACTIVE);
        return destinationRepository.save(destination);
    }

    private void createAttraction(Destination destination, String name, String type, int displayOrder) {
        Attraction attraction = new Attraction();
        attraction.setDestination(destination);
        attraction.setName(name);
        attraction.setType(type);
        attraction.setDisplayOrder(displayOrder);
        attractionRepository.save(attraction);
    }

    @Test
    @DisplayName("1. Create Attraction and verify relationship to Destination")
    void testCreateAttraction() {
        AttractionCreateRequest req = new AttractionCreateRequest();
        req.setName("Red Mosque");
        req.setType("HERITAGE");
        req.setShortDescription("Historic Jami Ul-Alfar Mosque in Pettah.");
        req.setLatitude(6.9387);
        req.setLongitude(79.8517);
        req.setStatus(AttractionStatus.ACTIVE);

        AttractionResponse created = attractionService.createAttraction(colomboId, req);

        assertNotNull(created.getId());
        assertEquals(colomboId, created.getDestinationId());
        assertEquals("Red Mosque", created.getName());
        assertEquals("HERITAGE", created.getType());
        assertEquals(6.9387, created.getLatitude());
        assertEquals(79.8517, created.getLongitude());
    }

    @Test
    @DisplayName("2. Retrieve attractions for Destination")
    void testGetAttractionsForDestination() {
        List<AttractionResponse> galleAttractions = attractionService.getAttractionsByDestinationId(galleId);
        assertNotNull(galleAttractions);
        assertEquals(4, galleAttractions.size());

        assertEquals("Galle Fort", galleAttractions.get(0).getName());
        assertEquals("Galle Lighthouse", galleAttractions.get(1).getName());
        assertEquals("Unawatuna Beach", galleAttractions.get(2).getName());
        assertEquals("Japanese Peace Pagoda", galleAttractions.get(3).getName());
    }

    @Test
    @DisplayName("3. Update Attraction details")
    void testUpdateAttraction() {
        List<AttractionResponse> galleAttractions = attractionService.getAttractionsByDestinationId(galleId);
        AttractionResponse first = galleAttractions.get(0);

        AttractionUpdateRequest update = new AttractionUpdateRequest();
        update.setShortDescription("Updated description for UNESCO Galle Fort.");

        AttractionResponse updated = attractionService.updateAttraction(galleId, first.getId(), update);
        assertEquals("Updated description for UNESCO Galle Fort.", updated.getShortDescription());
    }

    @Test
    @DisplayName("4. Delete Attraction and verify displayOrder re-indexing")
    void testDeleteAttraction() {
        List<AttractionResponse> listBefore = attractionService.getAttractionsByDestinationId(galleId);
        int initialSize = listBefore.size();
        AttractionResponse target = listBefore.get(1); // Second item (Galle Lighthouse)

        attractionService.deleteAttraction(galleId, target.getId());

        List<AttractionResponse> listAfter = attractionService.getAttractionsByDestinationId(galleId);
        assertEquals(initialSize - 1, listAfter.size());

        // Verify display orders are 0, 1, 2...
        for (int i = 0; i < listAfter.size(); i++) {
            assertEquals(i, listAfter.get(i).getDisplayOrder(), "Display order must be re-indexed cleanly to 0, 1, 2...");
        }

        assertThrows(ResourceNotFoundException.class, () -> {
            attractionService.getAttractionById(galleId, target.getId());
        });
    }
}
