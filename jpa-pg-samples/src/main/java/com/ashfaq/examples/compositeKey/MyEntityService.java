package com.ashfaq.examples.compositeKey;

import org.springframework.stereotype.Service;

@Service
public class MyEntityService {

    private final MyEntityRepository repository;

    public MyEntityService(MyEntityRepository repository) {
        this.repository = repository;
    }

    public boolean checkIfExists(Long id1, String id2) {
        MyEntityId key = new MyEntityId(id1, id2);
        return repository.existsById(key);
    }
}
