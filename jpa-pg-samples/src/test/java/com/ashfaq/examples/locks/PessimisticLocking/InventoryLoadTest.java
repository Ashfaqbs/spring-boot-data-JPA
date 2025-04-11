package com.ashfaq.examples.locks.PessimisticLocking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class InventoryLoadTest {

    private static final String URL = "http://localhost:8080/inventory/buy/1";

    @Test
    public void simulateTwoUsersBuying() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        RestTemplate restTemplate = new RestTemplate();

        Runnable task = () -> {
            String response = restTemplate.postForObject(URL, null, String.class);
            System.out.println(Thread.currentThread().getName() + " → " + response);
        };

        executor.submit(task);
        executor.submit(task);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}

