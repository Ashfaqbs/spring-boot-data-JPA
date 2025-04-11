package com.ashfaq.examples.locks.OptimisticLocking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StimulateController {

    @Autowired
    private ProductOLService productService;

    @PostMapping("/update-stock")
    public String simulateRaceCondition(@RequestParam Long productId, @RequestParam int quantity) {
        Runnable task = () -> {
            try {
                productService.updateStock(productId, quantity);
                System.out.println("Updated stock successfully");
            } catch (Exception e) {
                System.out.println("Update failed: " + e.getMessage());
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();

        return "Triggered two updates";
    }
}
