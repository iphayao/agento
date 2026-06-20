package com.bnpaper.agento.brand;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BrandProfileRepository extends JpaRepository<BrandProfile, UUID> {
}
