package com.lankastay.backend.mapper;

import com.lankastay.backend.dto.attraction.AttractionCreateRequest;
import com.lankastay.backend.dto.attraction.AttractionResponse;
import com.lankastay.backend.dto.attraction.AttractionUpdateRequest;
import com.lankastay.backend.entity.Attraction;
import com.lankastay.backend.entity.AttractionStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AttractionMapper {

    public Attraction toEntity(AttractionCreateRequest dto) {
        if (dto == null) return null;
        Attraction attraction = new Attraction();
        attraction.setName(dto.getName());
        attraction.setType(dto.getType());
        attraction.setShortDescription(dto.getShortDescription());
        attraction.setLatitude(dto.getLatitude());
        attraction.setLongitude(dto.getLongitude());
        attraction.setImage(dto.getImage());
        attraction.setEstimatedTravelTime(dto.getEstimatedTravelTime());
        attraction.setSource(dto.getSource());
        attraction.setSourceId(dto.getSourceId());
        attraction.setStatus(dto.getStatus() != null ? dto.getStatus() : AttractionStatus.ACTIVE);
        attraction.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0);
        return attraction;
    }

    public void updateEntity(Attraction attraction, AttractionUpdateRequest dto) {
        if (dto == null || attraction == null) return;
        if (dto.getName() != null) attraction.setName(dto.getName());
        if (dto.getType() != null) attraction.setType(dto.getType());
        if (dto.getShortDescription() != null) attraction.setShortDescription(dto.getShortDescription());
        if (dto.getLatitude() != null) attraction.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) attraction.setLongitude(dto.getLongitude());
        if (dto.getImage() != null) attraction.setImage(dto.getImage());
        if (dto.getEstimatedTravelTime() != null) attraction.setEstimatedTravelTime(dto.getEstimatedTravelTime());
        if (dto.getSource() != null) attraction.setSource(dto.getSource());
        if (dto.getSourceId() != null) attraction.setSourceId(dto.getSourceId());
        if (dto.getStatus() != null) attraction.setStatus(dto.getStatus());
        if (dto.getDisplayOrder() != null) attraction.setDisplayOrder(dto.getDisplayOrder());
    }

    public AttractionResponse toResponse(Attraction entity) {
        if (entity == null) return null;
        AttractionResponse dto = new AttractionResponse();
        dto.setId(entity.getId());
        dto.setDestinationId(entity.getDestination() != null ? entity.getDestination().getId() : null);
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setShortDescription(entity.getShortDescription());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setImage(entity.getImage());
        dto.setEstimatedTravelTime(entity.getEstimatedTravelTime());
        dto.setSource(entity.getSource());
        dto.setSourceId(entity.getSourceId());
        dto.setStatus(entity.getStatus());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public List<AttractionResponse> toResponseList(List<Attraction> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
