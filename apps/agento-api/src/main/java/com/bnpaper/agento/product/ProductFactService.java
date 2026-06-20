package com.bnpaper.agento.product;

import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFactService {

    private final ProductFactRepository repository;

    @Transactional(readOnly = true)
    public List<ProductFact> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public ProductFact findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductFact", id));
    }

    @Transactional
    public ProductFact create(ProductFactDto.Request request) {
        ProductFact fact = ProductFact.builder()
                .productName(request.getProductName())
                .sku(request.getSku())
                .sheetCount(request.getSheetCount())
                .ply(request.getPly())
                .packSize(request.getPackSize())
                .cartonSize(request.getCartonSize())
                .keyBenefits(request.getKeyBenefits())
                .proofPoints(request.getProofPoints())
                .build();
        return repository.save(fact);
    }

    @Transactional
    public ProductFact update(Long id, ProductFactDto.Request request) {
        ProductFact fact = findById(id);
        fact.setProductName(request.getProductName());
        fact.setSku(request.getSku());
        fact.setSheetCount(request.getSheetCount());
        fact.setPly(request.getPly());
        fact.setPackSize(request.getPackSize());
        fact.setCartonSize(request.getCartonSize());
        fact.setKeyBenefits(request.getKeyBenefits());
        fact.setProofPoints(request.getProofPoints());
        return repository.save(fact);
    }

    @Transactional
    public void delete(Long id) {
        ProductFact fact = findById(id);
        repository.delete(fact);
    }
}
