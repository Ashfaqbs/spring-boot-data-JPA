package com.ashfaq.examples.locks.OptimisticLocking;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductOL, Long> {
}
