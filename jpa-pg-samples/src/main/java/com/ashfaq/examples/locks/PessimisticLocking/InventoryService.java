package com.ashfaq.examples.locks.PessimisticLocking;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository repository;

    @Transactional
    public String purchaseItem(Long id) {
        InventoryItem item = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getQuantity() <= 0) {
            return "Item out of stock";
        }

        item.setQuantity(item.getQuantity() - 1);
        repository.save(item);

        return "Purchase successful";
    }
}
