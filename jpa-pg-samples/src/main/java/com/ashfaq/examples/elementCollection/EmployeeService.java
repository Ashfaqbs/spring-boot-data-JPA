package com.ashfaq.examples.elementCollection;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public void testEmployeeQuery() {
        // Add sample employees
//        Employee emp1 = new Employee();
//        emp1.setName("John Doe");
//        emp1.setEmpCode(101);
//        emp1.setSkills(Arrays.asList("Java", "Spring Boot"));
//
//        Employee emp2 = new Employee();
//        emp2.setName("Jane Smith");
//        emp2.setEmpCode(102);
//        emp2.setSkills(Arrays.asList("React", "Node.js"));
//
//        Employee emp3 = new Employee();
//        emp3.setName("Emily Davis");
//        emp3.setEmpCode(101); // Same empCode as emp1
//        emp3.setSkills(Arrays.asList("Java", "Docker"));
//
//        employeeRepository.saveAll(Arrays.asList(emp1, emp2, emp3));
//
//        // Query employees with empCode = 101 and skill = "Java"
//        List<Employee> employees = employeeRepository.findByEmpCodeAndSkill(101, "Java");
//
//        // Print the results
//        employees.forEach(employee -> {
//            System.out.println("Employee ID: " + employee.getId());
//            System.out.println("Employee Name: " + employee.getName());
//            System.out.println("Employee Skills: " + employee.getSkills());
//        });

        /*
            O/P
        Employee ID: 1
Employee Name: John Doe
Employee Skills: [Java, Spring Boot]
Employee ID: 3
Employee Name: Emily Davis
Employee Skills: [Java, Docker]
         */

    }
}

