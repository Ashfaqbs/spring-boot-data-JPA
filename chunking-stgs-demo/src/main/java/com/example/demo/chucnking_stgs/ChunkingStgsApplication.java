package com.example.demo.chucnking_stgs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.demo.chucnking_stgs.service.DataTransferService;

@SpringBootApplication
public class ChunkingStgsApplication   implements CommandLineRunner{

@Autowired
private  DataTransferService service;

    public static void main(String[] args) {
        SpringApplication.run(ChunkingStgsApplication.class, args);
    }

    @Override
    public void run(String... args) {
        service.transferData();
    }
}
