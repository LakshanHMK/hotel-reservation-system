package com.lankastay.backend.mapper;

import com.lankastay.backend.dto.destination.DestinationCreateRequest;
import com.lankastay.backend.dto.destination.DestinationResponse;
import com.lankastay.backend.dto.destination.DestinationSummaryResponse;
import com.lankastay.backend.dto.destination.DestinationUpdateRequest;
import com.lankastay.backend.entity.Attraction;
import com.lankastay.backend.entity.Destination;
import com.lankastay.backend.entity.DestinationStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DestinationMapper {

    private final AttractionMapper attractionMapper;

    public DestinationMapper(AttractionMapper attractionMapper) {
        this.attractionMapper = attractionMapper;
    }

    public Destination toEntity(DestinationCreateRequest dto) {
        if (dto == null) return null;
        Destination destination = new Destination();
        destination.setName(dto.getName());
        destination.setSlug(dto.getSlug());
        destination.setShortDescription(dto.getShortDescription());
        destination.setFullDescription(dto.getFullDescription());
        destination.setCategory(dto.getCategory());
        destination.setRegion(dto.getRegion());
        destination.setDistrict(dto.getDistrict());
        destination.setLatitude(dto.getLatitude());
        destination.setLongitude(dto.getLongitude());
        destination.setStatus(dto.getStatus() != null ? dto.getStatus() : DestinationStatus.DRAFT);
        destination.setMainImage(dto.getMainImage());
        if (dto.getCardImagePosition() != null) destination.setCardImagePosition(dto.getCardImagePosition());
        if (dto.getHeroImagePosition() != null) destination.setHeroImagePosition(dto.getHeroImagePosition());
        if (dto.getHeroFitMode() != null) destination.setHeroFitMode(dto.getHeroFitMode());
        if (dto.getLastSavedStep() != null) destination.setLastSavedStep(dto.getLastSavedStep());
        if (dto.getLastCompletedStep() != null) destination.setLastCompletedStep(dto.getLastCompletedStep());
        destination.setLastUpdatedSection(dto.getLastUpdatedSection());

        if (dto.getThemeKeys() != null) {
            destination.setThemeKeys(new HashSet<>(dto.getThemeKeys()));
        }
        if (dto.getHighlights() != null) {
            destination.setHighlights(new ArrayList<>(dto.getHighlights()));
        }
        if (dto.getAttractions() != null && !dto.getAttractions().isEmpty()) {
            List<Attraction> attractions = dto.getAttractions().stream()
                    .map(attractionMapper::toEntity)
                    .peek(a -> a.setDestination(destination))
                    .collect(Collectors.toList());
            destination.setAttractions(attractions);
        }

        return destination;
    }

    public void updateEntity(Destination destination, DestinationUpdateRequest dto) {
        if (dto == null || destination == null) return;

        if (dto.getName() != null) destination.setName(dto.getName());
        if (dto.getSlug() != null) destination.setSlug(dto.getSlug());
        if (dto.getShortDescription() != null) destination.setShortDescription(dto.getShortDescription());
        if (dto.getFullDescription() != null) destination.setFullDescription(dto.getFullDescription());
        if (dto.getCategory() != null) destination.setCategory(dto.getCategory());
        if (dto.getRegion() != null) destination.setRegion(dto.getRegion());
        if (dto.getDistrict() != null) destination.setDistrict(dto.getDistrict());
        if (dto.getLatitude() != null) destination.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) destination.setLongitude(dto.getLongitude());
        if (dto.getStatus() != null) destination.setStatus(dto.getStatus());
        if (dto.getMainImage() != null) destination.setMainImage(dto.getMainImage());
        if (dto.getCardImagePosition() != null) destination.setCardImagePosition(dto.getCardImagePosition());
        if (dto.getHeroImagePosition() != null) destination.setHeroImagePosition(dto.getHeroImagePosition());
        if (dto.getHeroFitMode() != null) destination.setHeroFitMode(dto.getHeroFitMode());
        if (dto.getLastSavedStep() != null) destination.setLastSavedStep(dto.getLastSavedStep());
        if (dto.getLastCompletedStep() != null) destination.setLastCompletedStep(dto.getLastCompletedStep());
        if (dto.getLastUpdatedSection() != null) destination.setLastUpdatedSection(dto.getLastUpdatedSection());

        if (dto.getThemeKeys() != null) {
            destination.setThemeKeys(new HashSet<>(dto.getThemeKeys()));
        }
        if (dto.getHighlights() != null) {
            destination.setHighlights(new ArrayList<>(dto.getHighlights()));
        }

        if (dto.getAttractions() != null) {
            destination.getAttractions().clear();
            List<Attraction> updatedAttractions = dto.getAttractions().stream()
                    .map(attractionMapper::toEntity)
                    .peek(a -> a.setDestination(destination))
                    .collect(Collectors.toList());
            destination.getAttractions().addAll(updatedAttractions);
        }
    }

    public DestinationResponse toResponse(Destination entity) {
        if (entity == null) return null;
        DestinationResponse dto = new DestinationResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setSlug(entity.getSlug());
        dto.setShortDescription(entity.getShortDescription());
        dto.setFullDescription(entity.getFullDescription());
        dto.setCategory(entity.getCategory());
        dto.setRegion(entity.getRegion());
        dto.setDistrict(entity.getDistrict());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setStatus(entity.getStatus());
        dto.setActive(entity.getActive());
        dto.setMainImage(entity.getMainImage());
        dto.setCardImagePosition(entity.getCardImagePosition());
        dto.setHeroImagePosition(entity.getHeroImagePosition());
        dto.setHeroFitMode(entity.getHeroFitMode());
        dto.setLastSavedStep(entity.getLastSavedStep());
        dto.setLastCompletedStep(entity.getLastCompletedStep());
        dto.setLastUpdatedSection(entity.getLastUpdatedSection());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setVersion(entity.getVersion());

        if (entity.getThemeKeys() != null) {
            dto.setThemeKeys(new HashSet<>(entity.getThemeKeys()));
        }
        if (entity.getHighlights() != null) {
            dto.setHighlights(new ArrayList<>(entity.getHighlights()));
        }
        if (entity.getAttractions() != null) {
            dto.setAttractions(attractionMapper.toResponseList(entity.getAttractions()));
        }

        return dto;
    }

    public DestinationSummaryResponse toSummaryResponse(Destination entity) {
        if (entity == null) return null;
        DestinationSummaryResponse dto = new DestinationSummaryResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setSlug(entity.getSlug());
        dto.setShortDescription(entity.getShortDescription());
        dto.setCategory(entity.getCategory());
        dto.setRegion(entity.getRegion());
        dto.setDistrict(entity.getDistrict());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setStatus(entity.getStatus());
        dto.setActive(entity.getActive());
        dto.setMainImage(entity.getMainImage());
        if (entity.getThemeKeys() != null) {
            dto.setThemeKeys(new HashSet<>(entity.getThemeKeys()));
        }
        return dto;
    }

    public List<DestinationResponse> toResponseList(List<Destination> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<DestinationSummaryResponse> toSummaryResponseList(List<Destination> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toSummaryResponse).collect(Collectors.toList());
    }
}
