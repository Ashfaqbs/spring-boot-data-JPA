package com.ashfaq.examples.locks.PessimisticLocking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService service;

    @PostMapping("/buy/{id}")
    public ResponseEntity<String> buyItem(@PathVariable Long id) {
        String result = service.purchaseItem(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/simulate")
    public ResponseEntity<String> simulateTwoUsersBuying() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/inventory/buy/1";

        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            futures.add(executor.submit(() -> {
                try {
                    return restTemplate.postForObject(url, null, String.class);
                } catch (Exception e) {
                    return "Error: " + e.getMessage();
                }
            }));
        }

        executor.shutdown();

        StringBuilder result = new StringBuilder();
        for (Future<String> future : futures) {
            try {
                result.append(future.get()).append("\n");
            } catch (Exception e) {
                result.append("Thread error: ").append(e.getMessage()).append("\n");
            }
        }

        return ResponseEntity.ok(result.toString());
    }

}
