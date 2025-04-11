package com.ashfaq.examples.locks.OptimisticLocking;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductOLService {

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public void updateStock(Long productId, int quantityToAdd) {
        ProductOL product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setStock(product.getStock() + quantityToAdd);
        productRepository.save(product); // Will throw OptimisticLockException if versions clash
    }


}
