package com.example.demo.chucnking_stgs.repo;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.chucnking_stgs.model.SourceData;

@Repository
public interface SourceDataRepository extends JpaRepository<SourceData, Long> {
    List<SourceData> findAllBy(Pageable pageable);
}

