package backend.playsembly.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BringListRepository extends JpaRepository<BringList, Long> {
    Optional<BringList> findByEvent(Event event);
    
}