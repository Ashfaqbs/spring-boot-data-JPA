package com.ashfaq.examples.compositeKey;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/entity")
public class MyEntityController {

    private final MyEntityService service;

    public MyEntityController(MyEntityService service) {
        this.service = service;
    }

    @GetMapping("/exists")
    public boolean checkExists(@RequestParam Long id1, @RequestParam String id2) {
        return service.checkIfExists(id1, id2);
    }
}
