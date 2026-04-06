package backend.playsembly.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import backend.playsembly.domain.bgg.BoardGame;
import java.util.Optional;

public interface WantListItemRepository extends JpaRepository<WantListItem, Long> {
    boolean existsByWantListAndBoardgame(WantList wantList, BoardGame boardgame);
    Optional<WantListItem> findByWantListAndBoardgame(WantList wantList, BoardGame boardgame);
    Optional<WantListItem> findWantListByUserAndBoardgame(AppUser user, BoardGame boardgame);

    Optional<WantListItem> findByWantListAndUserAndBoardgame(WantList wantList, AppUser user, BoardGame boardGame);
    boolean existsByUserAndEventAndBoardgame(AppUser user, Event event, BoardGame game);
}