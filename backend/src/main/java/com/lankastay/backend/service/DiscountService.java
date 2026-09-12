package com.lankastay.backend.service;

import com.lankastay.backend.entity.Discount;
import com.lankastay.backend.repository.DiscountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;

    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Transactional(readOnly = true)
    public List<Discount> getAllDiscounts(Long hotelId) {
        if (hotelId == null) {
            return discountRepository.findAll();
        }
        return discountRepository.findByHotelId(hotelId);
    }

    @Transactional(readOnly = true)
    public Optional<Discount> getDiscountById(Long id) {
        return discountRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Discount> getDiscountByCode(String code) {
        return discountRepository.findByCode(code);
    }

    @Transactional(readOnly = true)
    public Optional<Discount> validatePromoCode(String code, Long hotelId, Integer nights) {
        return discountRepository.findByCode(code)
                .filter(d -> "ACTIVE".equalsIgnoreCase(d.getStatus()))
                .filter(d -> d.getHotelId() == null || d.getHotelId().equals(hotelId))
                .filter(d -> nights == null || d.getMinimumNights() == null || nights >= d.getMinimumNights())
                .filter(d -> {
                    LocalDate today = LocalDate.now();
                    boolean afterStart = d.getValidFrom() == null || !today.isBefore(d.getValidFrom());
                    boolean beforeEnd = d.getValidTo() == null || !today.isAfter(d.getValidTo());
                    return afterStart && beforeEnd;
                });
    }

    @Transactional
    public Discount createDiscount(Discount discount) {
        return discountRepository.save(discount);
    }

    @Transactional
    public Discount updateDiscount(Long id, Discount updated) {
        Discount existing = discountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discount not found with ID: " + id));

        existing.setCode(updated.getCode());
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setDiscountType(updated.getDiscountType());
        existing.setDiscountValue(updated.getDiscountValue());
        existing.setMinimumNights(updated.getMinimumNights());
        existing.setValidFrom(updated.getValidFrom());
        existing.setValidTo(updated.getValidTo());
        existing.setStatus(updated.getStatus());

        return discountRepository.save(existing);
    }

    @Transactional
    public void deleteDiscount(Long id) {
        discountRepository.deleteById(id);
    }
}
