package backend.playsembly.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface WantListRepository extends CrudRepository<WantListItem, Long> {
    List<WantListItem> findByEvent(Event event);
}