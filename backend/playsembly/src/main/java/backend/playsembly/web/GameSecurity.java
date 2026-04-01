package backend.playsembly.web;

import org.springframework.stereotype.Component;

import backend.playsembly.domain.AppUser;
import backend.playsembly.domain.GameSessionRepository;

@Component
public class GameSecurity {

    private final GameSessionRepository gameSessionRepository;

    public GameSecurity(GameSessionRepository gameSessionRepository) {
        this.gameSessionRepository = gameSessionRepository;
    }

    public boolean isCreatorOrAdmin(Long gameId, AppUser principal) {
        return gameSessionRepository.findById(gameId)
            .map(game -> game.getCreator().getId().equals(principal.getId())
                        || principal.getRole().equals("MODERATOR")
                        || principal.getRole().equals("ADMIN"))
            .orElse(false);
    }
}