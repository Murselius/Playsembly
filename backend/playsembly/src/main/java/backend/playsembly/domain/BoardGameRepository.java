package backend.playsembly.domain;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import backend.playsembly.domain.bgg.BoardGame;

public interface BoardGameRepository extends CrudRepository<BoardGame, Long> {
    Optional<BoardGame> findByBggId(Long bggId);
}
