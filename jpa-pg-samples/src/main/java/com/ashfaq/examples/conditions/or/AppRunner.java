package com.ashfaq.examples.conditions.or;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppRunner implements CommandLineRunner {

    private final IncidentService incidentService;

    public AppRunner(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @Override
    public void run(String... args) {
//        incidentService.testQuery();
    }
}
