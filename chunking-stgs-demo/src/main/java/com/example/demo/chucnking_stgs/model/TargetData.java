package com.example.demo.chucnking_stgs.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sample_data", schema = "target_schema")
@Getter@Setter@AllArgsConstructor@NoArgsConstructor
public class TargetData {

    @Id
    private Long id;

    private String name;
    private String value;

    // getters and setters
}

