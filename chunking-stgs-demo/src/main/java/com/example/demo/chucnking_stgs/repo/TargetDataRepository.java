package com.example.demo.chucnking_stgs.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.chucnking_stgs.model.TargetData;

@Repository
public interface TargetDataRepository extends JpaRepository<TargetData, Long> { }
