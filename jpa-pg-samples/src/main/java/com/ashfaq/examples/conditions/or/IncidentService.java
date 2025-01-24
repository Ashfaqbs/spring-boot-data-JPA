package com.ashfaq.examples.conditions.or;


import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public void testQuery() {
        // Incident 1: Reopened ticket with today's date
//        Incident incident1 = new Incident();
//        incident1.setStatus("Reopened");
//        incident1.setCreatedDate(new Date());  // Current date
//
//        // Incident 2: Open ticket with creation date 6 days ago
//        Incident incident2 = new Incident();
//        incident2.setStatus("Open");
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.DATE, -6);  // 6 days ago
//        incident2.setCreatedDate(calendar.getTime());
//
//        // Incident 3: Closed ticket with creation date 2 days ago
//        Incident incident3 = new Incident();
//        incident3.setStatus("Closed");
//        calendar.add(Calendar.DATE, -4);  // 2 days ago
//        incident3.setCreatedDate(calendar.getTime());
//
//        // Incident 4: Open ticket with today's date
//        Incident incident4 = new Incident();
//        incident4.setStatus("Open");
//        incident4.setCreatedDate(new Date());  // Current date
//
//        // Assuming you have a repository for storing these incidents
//        incidentRepository.saveAll(Arrays.asList(incident1, incident2, incident3, incident4));
//
//        System.out.println("Sample data saved successfully!");



        // Query Incidents
        Calendar fiveDaysAgo = Calendar.getInstance();
        fiveDaysAgo.add(Calendar.DATE, -5);

        List<Incident> Incidents = incidentRepository.findIncidentsByStatusAndDate(
                "Reopened",
                "Open",
                fiveDaysAgo.getTime()
        );

        // Print results
        Incidents.forEach(Incident -> System.out.println("Incident ID: " + Incident.getId() + ", Status: " + Incident.getStatus()));

        /*
         O/P
        Sample data saved successfully!
         Incident ID: 5, Status: Reopened
        Incident ID: 6, Status: Open
         */


    }
}
