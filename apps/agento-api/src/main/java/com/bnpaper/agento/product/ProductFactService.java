package com.bnpaper.agento.product;

import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductFactService {

    private final ProductFactRepository repository;

    public List<ProductFactDto.Response> findAll() {
        return repository.findAll().stream()
                .map(ProductFactDto::toResponse)
                .collect(Collectors.toList());
    }

    public ProductFactDto.Response findById(UUID id) {
        return repository.findById(id)
                .map(ProductFactDto::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("ProductFact", id));
    }

    public List<ProductFact> findAllEntities() {
        return repository.findAll();
    }

    @Transactional
    public ProductFactDto.Response create(ProductFactDto.Request request) {
        ProductFact entity = ProductFact.builder()
                .productName(request.getProductName())
                .sku(request.getSku())
                .sheetCount(request.getSheetCount())
                .ply(request.getPly())
                .packSize(request.getPackSize())
                .cartonSize(request.getCartonSize())
                .keyBenefits(request.getKeyBenefits())
                .proofPoints(request.getProofPoints())
                .build();
        return ProductFactDto.toResponse(repository.save(entity));
    }

    @Transactional
    public ProductFactDto.Response update(UUID id, ProductFactDto.Request request) {
        ProductFact entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductFact", id));
        entity.setProductName(request.getProductName());
        entity.setSku(request.getSku());
        entity.setSheetCount(request.getSheetCount());
        entity.setPly(request.getPly());
        entity.setPackSize(request.getPackSize());
        entity.setCartonSize(request.getCartonSize());
        entity.setKeyBenefits(request.getKeyBenefits());
        entity.setProofPoints(request.getProofPoints());
        return ProductFactDto.toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("ProductFact", id);
        }
        repository.deleteById(id);
    }
}
