package com.ashfaq.examples.conditions.or;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    @Query("SELECT t FROM Incident t WHERE t.status = :statusReopened " +
            "OR (t.status = :statusOpen AND t.createdDate < :date)")
    List<Incident> findIncidentsByStatusAndDate(
            @Param("statusReopened") String statusReopened,
            @Param("statusOpen") String statusOpen,
            @Param("date") Date date
    );
}
