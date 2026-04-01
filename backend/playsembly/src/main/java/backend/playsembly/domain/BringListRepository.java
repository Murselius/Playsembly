package backend.playsembly.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface BringListRepository extends CrudRepository<BringListItem, Long> {
    List<BringListItem> findByEvent(Event event);
}