package backend.playsembly.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface GameSessionRepository extends CrudRepository<GameSession, Long>{


    @Query("SELECT g FROM GameSession g " +
           "LEFT JOIN FETCH g.boardgame " +
           "LEFT JOIN FETCH g.creator " +
           "LEFT JOIN FETCH g.participants " +
           "WHERE g.id = :id")
    Optional<GameSession> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT g FROM GameSession g " +
       "JOIN FETCH g.boardgame " +
       "LEFT JOIN FETCH g.creator " +
       "WHERE g.event = :event")
    List<GameSession> findByEvent(@Param("event") Event event);
    
}
