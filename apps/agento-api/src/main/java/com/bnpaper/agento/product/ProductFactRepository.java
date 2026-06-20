package com.bnpaper.agento.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductFactRepository extends JpaRepository<ProductFact, Long> {
}
