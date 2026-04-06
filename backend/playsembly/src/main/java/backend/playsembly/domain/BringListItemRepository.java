package backend.playsembly.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.playsembly.domain.bgg.BoardGame;

import java.util.List;
import java.util.Optional;

public interface BringListItemRepository extends JpaRepository<BringListItem, Long> {
    List<BringListItem> findByEvent(Event event);
    List<BringListItem> findByEventAndUser(Event event, AppUser user);
    List<BringListItem> findByEventAndBoardgame(Event event, BoardGame boardgame);

    Optional<BringListItem> findByEventAndUserAndBoardgame(Event event, AppUser user, BoardGame boardgame);
}