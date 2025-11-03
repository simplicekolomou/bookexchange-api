package ovh.bookexchange.api.infrastructures;

import org.springframework.data.repository.ListCrudRepository;
import ovh.bookexchange.api.domains.entities.TestEntity;

public interface TestRepository extends ListCrudRepository<TestEntity, Long> {
}
