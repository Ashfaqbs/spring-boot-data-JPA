package com.ashfaq.examples.compositeKey;

import java.io.Serializable;
import java.util.Objects;

public class MyEntityId implements Serializable {
    private Long id1;
    private String id2;

    public MyEntityId() {}

    public MyEntityId(Long id1, String id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    // Getters, Setters, equals(), and hashCode() are required for @IdClass
    public Long getId1() { return id1; }
    public void setId1(Long id1) { this.id1 = id1; }

    public String getId2() { return id2; }
    public void setId2(String id2) { this.id2 = id2; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyEntityId that = (MyEntityId) o;
        return Objects.equals(id1, that.id1) && Objects.equals(id2, that.id2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id1, id2);
    }
}
    
    // MyEntity.java 
    // This is the entity class that uses the composite key.