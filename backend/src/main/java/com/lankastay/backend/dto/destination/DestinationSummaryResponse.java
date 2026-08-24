package com.lankastay.backend.dto.destination;

import com.lankastay.backend.entity.DestinationStatus;
import java.util.HashSet;
import java.util.Set;

public class DestinationSummaryResponse {

    private Long id;
    private String name;
    private String slug;
    private String shortDescription;
    private String category;
    private String region;
    private String district;
    private Double latitude;
    private Double longitude;
    private DestinationStatus status;
    private Boolean active;
    private String mainImage;
    private String image;
    private Set<String> themeKeys = new HashSet<>();

    public DestinationSummaryResponse() {}

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

    public Set<String> getThemeKeys() {
        return themeKeys;
    }

    public void setThemeKeys(Set<String> themeKeys) {
        this.themeKeys = themeKeys;
    }
}
