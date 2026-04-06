package backend.playsembly.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WantListRepository extends JpaRepository<WantList, Long> {
    Optional<WantList> findByEvent(Event event);

    Optional<WantList> findByEventId(Long eventId);
}