package com.ashfaq.examples.elementCollection;


import jakarta.persistence.*;
import java.util.List;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary Key

    private String name; // Employee Name

    private int empCode; // Employee Code (Non-Unique)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "employee_skills", // Table to store skills
            joinColumns = @JoinColumn(name = "employee_id") // Join column
    )
    @Column(name = "skill") // Column for each skill
    private List<String> skills;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEmpCode() {
        return empCode;
    }

    public void setEmpCode(int empCode) {
        this.empCode = empCode;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
}

