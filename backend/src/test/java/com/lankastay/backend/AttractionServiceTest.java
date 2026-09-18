package com.lankastay.backend;

import com.lankastay.backend.dto.attraction.AttractionCreateRequest;
import com.lankastay.backend.dto.attraction.AttractionResponse;
import com.lankastay.backend.dto.attraction.AttractionUpdateRequest;
import com.lankastay.backend.entity.AttractionStatus;
import com.lankastay.backend.exception.ResourceNotFoundException;
import com.lankastay.backend.service.AttractionService;
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

        AttractionResponse created = attractionService.createAttraction(1L, req); // Destination 1 (Colombo)

        assertNotNull(created.getId());
        assertEquals(1L, created.getDestinationId());
        assertEquals("Red Mosque", created.getName());
        assertEquals("HERITAGE", created.getType());
        assertEquals(6.9387, created.getLatitude());
        assertEquals(79.8517, created.getLongitude());
    }

    @Test
    @DisplayName("2. Retrieve attractions for Destination")
    void testGetAttractionsForDestination() {
        List<AttractionResponse> galleAttractions = attractionService.getAttractionsByDestinationId(3L); // Galle
        assertNotNull(galleAttractions);
        assertTrue(galleAttractions.size() >= 4, "Galle seed attractions must be loaded");

        assertEquals("Galle Fort", galleAttractions.get(0).getName());
        assertEquals("Galle Lighthouse", galleAttractions.get(1).getName());
        assertEquals("Unawatuna Beach", galleAttractions.get(2).getName());
        assertEquals("Japanese Peace Pagoda", galleAttractions.get(3).getName());
    }

    @Test
    @DisplayName("3. Update Attraction details")
    void testUpdateAttraction() {
        List<AttractionResponse> galleAttractions = attractionService.getAttractionsByDestinationId(3L);
        AttractionResponse first = galleAttractions.get(0);

        AttractionUpdateRequest update = new AttractionUpdateRequest();
        update.setShortDescription("Updated description for UNESCO Galle Fort.");

        AttractionResponse updated = attractionService.updateAttraction(3L, first.getId(), update);
        assertEquals("Updated description for UNESCO Galle Fort.", updated.getShortDescription());
    }

    @Test
    @DisplayName("4. Delete Attraction and verify displayOrder re-indexing")
    void testDeleteAttraction() {
        List<AttractionResponse> listBefore = attractionService.getAttractionsByDestinationId(3L);
        int initialSize = listBefore.size();
        AttractionResponse target = listBefore.get(1); // Second item (Galle Lighthouse)

        attractionService.deleteAttraction(3L, target.getId());

        List<AttractionResponse> listAfter = attractionService.getAttractionsByDestinationId(3L);
        assertEquals(initialSize - 1, listAfter.size());

        // Verify display orders are 0, 1, 2...
        for (int i = 0; i < listAfter.size(); i++) {
            assertEquals(i, listAfter.get(i).getDisplayOrder(), "Display order must be re-indexed cleanly to 0, 1, 2...");
        }

        assertThrows(ResourceNotFoundException.class, () -> {
            attractionService.getAttractionById(3L, target.getId());
        });
    }
}
