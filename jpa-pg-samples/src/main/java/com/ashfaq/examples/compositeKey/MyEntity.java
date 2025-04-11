package com.ashfaq.examples.compositeKey;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(MyEntityId.class) // Reference the composite key class
@Table(name = "my_entity", schema = "labschema")
public class MyEntity {
    
    @Id
    private Long id1;

    @Id
    private String id2;

    private String name;

    public MyEntity() {}

    public MyEntity(Long id1, String id2, String name) {
        this.id1 = id1;
        this.id2 = id2;
        this.name = name;
    }

    // Getters and Setters
    public Long getId1() { return id1; }
    public void setId1(Long id1) { this.id1 = id1; }

    public String getId2() { return id2; }
    public void setId2(String id2) { this.id2 = id2; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
