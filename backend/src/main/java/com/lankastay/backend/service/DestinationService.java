package com.lankastay.backend.service;

import com.lankastay.backend.dto.destination.DestinationCreateRequest;
import com.lankastay.backend.dto.destination.DestinationResponse;
import com.lankastay.backend.dto.destination.DestinationSummaryResponse;
import com.lankastay.backend.dto.destination.DestinationUpdateRequest;
import com.lankastay.backend.entity.Destination;
import com.lankastay.backend.entity.DestinationStatus;
import com.lankastay.backend.exception.BusinessRuleException;
import com.lankastay.backend.exception.ConflictException;
import com.lankastay.backend.exception.ResourceNotFoundException;
import com.lankastay.backend.mapper.DestinationMapper;
import com.lankastay.backend.repository.DestinationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class DestinationService {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private final DestinationRepository destinationRepository;
    private final DestinationMapper destinationMapper;
    private final com.lankastay.backend.repository.HotelRepository hotelRepository;

    public DestinationService(DestinationRepository destinationRepository, DestinationMapper destinationMapper,
                              com.lankastay.backend.repository.HotelRepository hotelRepository) {
        this.destinationRepository = destinationRepository;
        this.destinationMapper = destinationMapper;
        this.hotelRepository = hotelRepository;
    }

    @Transactional(readOnly = true)
    public Page<DestinationSummaryResponse> getManagementDestinations(
            DestinationStatus status,
            String region,
            String district,
            String search,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Destination> pageResult = destinationRepository.findByManagementFilters(
                status,
                (region != null && !region.isBlank()) ? region : null,
                (district != null && !district.isBlank()) ? district : null,
                (search != null && !search.isBlank()) ? search : null,
                pageable
        );
        return pageResult.map(destinationMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public List<DestinationResponse> getAllManagementDestinationsList() {
        List<Destination> list = destinationRepository.findAll(Sort.by("id").ascending());
        return destinationMapper.toResponseList(list);
    }

    @Transactional(readOnly = true)
    public List<DestinationSummaryResponse> getActiveCustomerDestinations() {
        List<Destination> activeList = destinationRepository.findActiveDestinationsOrdered(DestinationStatus.ACTIVE);
        return destinationMapper.toSummaryResponseList(activeList);
    }

    @Transactional(readOnly = true)
    public DestinationResponse getDestinationById(Long id) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + id));
        return destinationMapper.toResponse(destination);
    }

    @Transactional(readOnly = true)
    public DestinationResponse getActiveCustomerDestinationBySlugOrId(String identifier) {
        Destination destination = null;

        // Try numeric ID lookup
        try {
            Long id = Long.parseLong(identifier);
            destination = destinationRepository.findById(id).orElse(null);
        } catch (NumberFormatException ignored) {}

        // Fallback to slug lookup
        if (destination == null) {
            destination = destinationRepository.findBySlug(identifier).orElse(null);
        }

        if (destination == null || destination.getStatus() != DestinationStatus.ACTIVE) {
            throw new ResourceNotFoundException("Active destination not found with identifier: " + identifier);
        }

        // Filter active attractions for customer response
        DestinationResponse response = destinationMapper.toResponse(destination);
        if (response.getAttractions() != null) {
            response.setAttractions(
                    response.getAttractions().stream()
                            .filter(a -> a.getStatus() == com.lankastay.backend.entity.AttractionStatus.ACTIVE)
                            .collect(Collectors.toList())
            );
        }
        return response;
    }

    public DestinationResponse createDestination(DestinationCreateRequest request) {
        validateCoordinates(request.getLatitude(), request.getLongitude());

        String slug = request.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = slugify(request.getName());
        }
        slug = ensureUniqueSlug(slug, null);

        Destination destination = destinationMapper.toEntity(request);
        destination.setSlug(slug);

        if (request.getStatus() == DestinationStatus.ACTIVE) {
            validateForActivation(destination);
        }

        Destination saved = destinationRepository.save(destination);
        return destinationMapper.toResponse(saved);
    }

    public DestinationResponse updateDestination(Long id, DestinationUpdateRequest request) {
        Destination existing = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + id));

        // Version check for optimistic locking
        if (request.getVersion() != null && existing.getVersion() != null
                && !request.getVersion().equals(existing.getVersion())) {
            throw new ConflictException("Optimistic locking failure: This Destination was updated by another manager concurrent edit. Stale version: "
                    + request.getVersion() + ", Current database version: " + existing.getVersion());
        }

        if (request.getLatitude() != null || request.getLongitude() != null) {
            Double lat = request.getLatitude() != null ? request.getLatitude() : existing.getLatitude();
            Double lng = request.getLongitude() != null ? request.getLongitude() : existing.getLongitude();
            validateCoordinates(lat, lng);
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            if (destinationRepository.existsByNameIgnoreCaseAndIdNot(request.getName().trim(), id)) {
                throw new BusinessRuleException("A Destination with name '" + request.getName().trim() + "' already exists.");
            }
        }

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = slugify(request.getSlug());
            if (destinationRepository.existsBySlugAndIdNot(newSlug, id)) {
                newSlug = ensureUniqueSlug(newSlug, id);
            }
            existing.setSlug(newSlug);
        }

        destinationMapper.updateEntity(existing, request);

        if (request.getStatus() == DestinationStatus.ACTIVE) {
            validateForActivation(existing);
        }

        Destination saved = destinationRepository.save(existing);
        return destinationMapper.toResponse(saved);
    }

    public DestinationResponse setStatus(Long id, DestinationStatus targetStatus) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + id));

        if (destination.getStatus() == targetStatus) {
            return destinationMapper.toResponse(destination);
        }

        if (targetStatus == DestinationStatus.ACTIVE) {
            validateForActivation(destination);
        }

        destination.setStatus(targetStatus);
        destination.setLastUpdatedSection("Visibility Status");
        Destination saved = destinationRepository.save(destination);
        return destinationMapper.toResponse(saved);
    }

    public void deleteDestination(Long id) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + id));

        if (hasLinkedHotels(id)) {
            throw new BusinessRuleException("Cannot permanently delete Destination '" + destination.getName() +
                    "' because it is currently linked to one or more Hotels. Reassign or remove linked Hotels first.");
        }

        destinationRepository.delete(destination);
    }

    public boolean hasLinkedHotels(Long destinationId) {
        if (destinationId == null) return false;
        return !hotelRepository.findByDestinationId(destinationId).isEmpty();
    }

    private void validateForActivation(Destination d) {
        if (d.getName() == null || d.getName().isBlank()) {
            throw new BusinessRuleException("Destination Name is required for activation.");
        }
        if (d.getShortDescription() == null || d.getShortDescription().isBlank()) {
            throw new BusinessRuleException("Short Description is required for activation.");
        }
        if (d.getFullDescription() == null || d.getFullDescription().isBlank()) {
            throw new BusinessRuleException("Full Description is required for activation.");
        }
        if (d.getThemeKeys() == null || d.getThemeKeys().isEmpty()) {
            throw new BusinessRuleException("Select at least one Travel Theme for activation.");
        }
        if (d.getRegion() == null || d.getRegion().isBlank()) {
            throw new BusinessRuleException("Province / Region is required for activation.");
        }
        if (d.getDistrict() == null || d.getDistrict().isBlank()) {
            throw new BusinessRuleException("District is required for activation.");
        }
        if (d.getLatitude() == null || d.getLongitude() == null) {
            throw new BusinessRuleException("Destination center location coordinates are required for activation.");
        }
        if (d.getMainImage() == null || d.getMainImage().isBlank()) {
            throw new BusinessRuleException("Main Cover Image is required for activation.");
        }
    }

    private void validateCoordinates(Double lat, Double lng) {
        if (lat != null && (lat < -90.0 || lat > 90.0)) {
            throw new BusinessRuleException("Latitude must be between -90.0 and 90.0 degrees.");
        }
        if (lng != null && (lng < -180.0 || lng > 180.0)) {
            throw new BusinessRuleException("Longitude must be between -180.0 and 180.0 degrees.");
        }
    }

    private String slugify(String input) {
        if (input == null || input.isBlank()) return "destination";
        String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = slug.replaceAll("-+", "-").replaceAll("(^-|-$)", "").toLowerCase(Locale.ENGLISH);
        return slug.isEmpty() ? "destination" : slug;
    }

    private String ensureUniqueSlug(String baseSlug, Long excludeId) {
        String slug = baseSlug;
        int suffix = 2;
        while (excludeId == null ? destinationRepository.existsBySlug(slug)
                                 : destinationRepository.existsBySlugAndIdNot(slug, excludeId)) {
            slug = baseSlug + "-" + (suffix++);
        }
        return slug;
    }
}
