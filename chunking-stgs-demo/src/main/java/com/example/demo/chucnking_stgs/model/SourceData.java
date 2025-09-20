package com.example.demo.chucnking_stgs.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sample_data", schema = "source_schema")
@Getter@Setter@AllArgsConstructor@NoArgsConstructor
public class SourceData {

    @Id
    private Long id;

    private String name;
    private String value;


}