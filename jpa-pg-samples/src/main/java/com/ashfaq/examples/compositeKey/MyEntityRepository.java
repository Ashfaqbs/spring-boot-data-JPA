package com.ashfaq.examples.compositeKey;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MyEntityRepository extends JpaRepository<MyEntity, MyEntityId> {
}
