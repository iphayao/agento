package com.bnpaper.agento.brand;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandProfileRepository extends JpaRepository<BrandProfile, Long> {

    Optional<BrandProfile> findTopByOrderByIdAsc();
}
