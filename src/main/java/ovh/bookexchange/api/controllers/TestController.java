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
    TestEntity newTest(@RequestBody TestRepresentation value) {
        return repository.save(new TestEntity(value.value));
    }

    @PutMapping("/test/{id}")
    TestEntity replaceTestEntity(@RequestBody TestRepresentation value, @PathVariable long id) {
        return repository.findById(id).map(testEntity -> {
            testEntity.setValue(value.value);
            return repository.save(testEntity);
        }).orElseGet(()->{
            return repository.save(new TestEntity(value.value));
        });
    }

    @DeleteMapping("/test/{id}")
    void deleteTestEntity(@PathVariable long id) {
        repository.deleteById(id);
    }
}
