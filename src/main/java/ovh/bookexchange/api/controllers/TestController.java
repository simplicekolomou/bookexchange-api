package ovh.bookexchange.api.controllers;

import org.springframework.web.bind.annotation.*;
import ovh.bookexchange.api.controllers.representations.TestRepresentation;
import ovh.bookexchange.api.domains.entities.TestEntity;
import ovh.bookexchange.api.infrastructures.TestRepository;

import java.util.List;

@RestController
public class TestController {
    private final TestRepository repository;
    public TestController(TestRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/test")
    List<TestEntity> all() {
        return repository.findAll();
    }

    @PostMapping("/test")
    void newTest(@RequestBody TestRepresentation value) {
        repository.save(new TestEntity(value.value));
    }


}
