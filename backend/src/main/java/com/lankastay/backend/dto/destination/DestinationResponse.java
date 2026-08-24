package com.lankastay.backend.dto.destination;

import com.lankastay.backend.dto.attraction.AttractionResponse;
import com.lankastay.backend.entity.DestinationStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DestinationResponse {

    private Long id;
    private String name;
    private String slug;
    private String shortDescription;
    private String fullDescription;
    private String category;
    private String region;
    private String district;
    private Double latitude;
    private Double longitude;
    private DestinationStatus status;
    private Boolean active;
    private String mainImage;
    private String image; // Frontend alias for mainImage
    private String cardImagePosition;
    private String heroImagePosition;
    private String heroFitMode;

    private Integer lastSavedStep;
    private Integer lastCompletedStep;
    private String lastUpdatedSection;

    private Instant createdAt;
    private Instant updatedAt;
    private Long version;

    private Set<String> themeKeys = new HashSet<>();
    private List<String> highlights = new ArrayList<>();
    private List<AttractionResponse> attractions = new ArrayList<>();

    public DestinationResponse() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public DestinationStatus getStatus() {
        return status;
    }

    public void setStatus(DestinationStatus status) {
        this.status = status;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
        this.image = mainImage;
    }

    public String getImage() {
        return image != null ? image : mainImage;
    }

    public void setImage(String image) {
        this.image = image;
        if (this.mainImage == null) {
            this.mainImage = image;
        }
    }

    public String getCardImagePosition() {
        return cardImagePosition;
    }

    public void setCardImagePosition(String cardImagePosition) {
        this.cardImagePosition = cardImagePosition;
    }

    public String getHeroImagePosition() {
        return heroImagePosition;
    }

    public void setHeroImagePosition(String heroImagePosition) {
        this.heroImagePosition = heroImagePosition;
    }

    public String getHeroFitMode() {
        return heroFitMode;
    }

    public void setHeroFitMode(String heroFitMode) {
        this.heroFitMode = heroFitMode;
    }

    public Integer getLastSavedStep() {
        return lastSavedStep;
    }

    public void setLastSavedStep(Integer lastSavedStep) {
        this.lastSavedStep = lastSavedStep;
    }

    public Integer getLastCompletedStep() {
        return lastCompletedStep;
    }

    public void setLastCompletedStep(Integer lastCompletedStep) {
        this.lastCompletedStep = lastCompletedStep;
    }

    public String getLastUpdatedSection() {
        return lastUpdatedSection;
    }

    public void setLastUpdatedSection(String lastUpdatedSection) {
        this.lastUpdatedSection = lastUpdatedSection;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Set<String> getThemeKeys() {
        return themeKeys;
    }

    public void setThemeKeys(Set<String> themeKeys) {
        this.themeKeys = themeKeys;
    }

    public List<String> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<String> highlights) {
        this.highlights = highlights;
    }

    public List<AttractionResponse> getAttractions() {
        return attractions;
    }

    public void setAttractions(List<AttractionResponse> attractions) {
        this.attractions = attractions;
    }
}
