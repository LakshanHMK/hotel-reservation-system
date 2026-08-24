package com.lankastay.backend.service;

import com.lankastay.backend.dto.attraction.AttractionCreateRequest;
import com.lankastay.backend.dto.attraction.AttractionResponse;
import com.lankastay.backend.dto.attraction.AttractionUpdateRequest;
import com.lankastay.backend.entity.Attraction;
import com.lankastay.backend.entity.AttractionStatus;
import com.lankastay.backend.entity.Destination;
import com.lankastay.backend.exception.ResourceNotFoundException;
import com.lankastay.backend.mapper.AttractionMapper;
import com.lankastay.backend.repository.AttractionRepository;
import com.lankastay.backend.repository.DestinationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AttractionService {

    private final AttractionRepository attractionRepository;
    private final DestinationRepository destinationRepository;
    private final AttractionMapper attractionMapper;

    public AttractionService(
            AttractionRepository attractionRepository,
            DestinationRepository destinationRepository,
            AttractionMapper attractionMapper
    ) {
        this.attractionRepository = attractionRepository;
        this.destinationRepository = destinationRepository;
        this.attractionMapper = attractionMapper;
    }

    @Transactional(readOnly = true)
    public List<AttractionResponse> getAttractionsByDestinationId(Long destinationId) {
        if (!destinationRepository.existsById(destinationId)) {
            throw new ResourceNotFoundException("Destination not found with id: " + destinationId);
        }
        List<Attraction> list = attractionRepository.findByDestinationIdOrderByDisplayOrderAsc(destinationId);
        return attractionMapper.toResponseList(list);
    }

    @Transactional(readOnly = true)
    public List<AttractionResponse> getActiveAttractionsByDestinationId(Long destinationId) {
        if (!destinationRepository.existsById(destinationId)) {
            throw new ResourceNotFoundException("Destination not found with id: " + destinationId);
        }
        List<Attraction> list = attractionRepository.findByDestinationIdAndStatusOrderByDisplayOrderAsc(destinationId, AttractionStatus.ACTIVE);
        return attractionMapper.toResponseList(list);
    }

    @Transactional(readOnly = true)
    public AttractionResponse getAttractionById(Long destinationId, Long attractionId) {
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction not found with id: " + attractionId));

        if (!attraction.getDestination().getId().equals(destinationId)) {
            throw new ResourceNotFoundException("Attraction " + attractionId + " does not belong to destination " + destinationId);
        }

        return attractionMapper.toResponse(attraction);
    }

    public AttractionResponse createAttraction(Long destinationId, AttractionCreateRequest request) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + destinationId));

        Attraction attraction = attractionMapper.toEntity(request);
        attraction.setDestination(destination);

        List<Attraction> existing = attractionRepository.findByDestinationIdOrderByDisplayOrderAsc(destinationId);
        if (request.getDisplayOrder() == null || request.getDisplayOrder() == 0) {
            attraction.setDisplayOrder(existing.size());
        }

        Attraction saved = attractionRepository.save(attraction);
        return attractionMapper.toResponse(saved);
    }

    public AttractionResponse updateAttraction(Long destinationId, Long attractionId, AttractionUpdateRequest request) {
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction not found with id: " + attractionId));

        if (!attraction.getDestination().getId().equals(destinationId)) {
            throw new ResourceNotFoundException("Attraction " + attractionId + " does not belong to destination " + destinationId);
        }

        attractionMapper.updateEntity(attraction, request);
        Attraction saved = attractionRepository.save(attraction);
        return attractionMapper.toResponse(saved);
    }

    public void deleteAttraction(Long destinationId, Long attractionId) {
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction not found with id: " + attractionId));

        if (!attraction.getDestination().getId().equals(destinationId)) {
            throw new ResourceNotFoundException("Attraction " + attractionId + " does not belong to destination " + destinationId);
        }

        attractionRepository.delete(attraction);

        // Re-index display orders for remaining attractions
        List<Attraction> remaining = attractionRepository.findByDestinationIdOrderByDisplayOrderAsc(destinationId);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setDisplayOrder(i);
        }
        attractionRepository.saveAll(remaining);
    }
}
