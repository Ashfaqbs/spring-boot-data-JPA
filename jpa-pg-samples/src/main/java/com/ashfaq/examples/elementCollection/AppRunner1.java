package com.ashfaq.examples.elementCollection;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppRunner1 implements CommandLineRunner {

    private final EmployeeService employeeService;

    public AppRunner1(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public void run(String... args) {
        employeeService.testEmployeeQuery();
    }
}
