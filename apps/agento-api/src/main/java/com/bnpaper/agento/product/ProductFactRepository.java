package com.bnpaper.agento.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductFactRepository extends JpaRepository<ProductFact, UUID> {
}
