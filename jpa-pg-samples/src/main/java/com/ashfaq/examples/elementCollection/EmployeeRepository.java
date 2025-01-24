package com.ashfaq.examples.elementCollection;


import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e JOIN e.skills s WHERE e.empCode = :empCode AND s = :skill")
    List<Employee> findByEmpCodeAndSkill(
            @Param("empCode") int empCode,
            @Param("skill") String skill
    );
}


