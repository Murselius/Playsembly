package backend.playsembly.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import backend.playsembly.domain.AppUser;
import backend.playsembly.domain.BoardGameRepository;
import backend.playsembly.domain.Event;
import backend.playsembly.domain.EventRepository;
import backend.playsembly.domain.GameSession;
import backend.playsembly.domain.GameSessionRepository;
import backend.playsembly.domain.bgg.BggService;
import backend.playsembly.domain.bgg.BoardGame;
import backend.playsembly.dto.GameSessionCreateDTO;
import backend.playsembly.dto.GameSessionDTO;
import backend.playsembly.dto.GameSessionUpdateDTO;

@RestController
@RequestMapping("/api/games")
public class RestGameSessionController {

    private final BggService bggService;
    private final EventRepository eventRepository;
    private final BoardGameRepository boardGameRepository;
    private final GameSessionRepository gameSessionRepository;

    public RestGameSessionController(GameSessionRepository gameSessionRepository, BoardGameRepository boardGameRepository, EventRepository eventRepository, BggService bggService) {
        this.gameSessionRepository = gameSessionRepository;
        this.boardGameRepository = boardGameRepository;
        this.eventRepository = eventRepository;
        this.bggService = bggService;
    }

    //GET game session
    @GetMapping("/{id}")
    public GameSessionDTO getGame(@PathVariable Long id) {
        GameSession game = gameSessionRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        //Estetään ikuinen JSON loop ja arkaluontoisen datan paljastaminen yksinkertaisella DTO -mallilla.
        GameSessionDTO dto = new GameSessionDTO();
        dto.setId(game.getId());
        dto.setDescription(game.getDescription());
        dto.setMinPlayers(game.getMinPlayers());
        dto.setMaxPlayers(game.getMaxPlayers());
        dto.setEstimatedDuration(game.getEstimatedDuration());
        dto.setStartTime(game.getStartTime());

        GameSessionDTO.BoardGameDTO bg = new GameSessionDTO.BoardGameDTO();
        bg.setBggId(game.getBoardgame().getBggId());
        bg.setName(game.getBoardgame().getName());
        bg.setImageUrl(game.getBoardgame().getImageUrl());
        bg.setMinPlayers(game.getBoardgame().getMinPlayers());
        bg.setMaxPlayers(game.getBoardgame().getMaxPlayers());
        dto.setBoardgame(bg);

        GameSessionDTO.AppUserDTO creator = new GameSessionDTO.AppUserDTO();
        creator.setId(game.getCreator().getId());
        creator.setUsername(game.getCreator().getUsername());
        dto.setCreator(creator);

        return dto;
    }

    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('MODERATOR','ADMIN')")
    public GameSessionDTO createGame(@RequestBody GameSessionCreateDTO dto,
                                    @AuthenticationPrincipal AppUser currentUser) throws Exception {

        // Hae tai tuo boardgame
        BoardGame boardGame = boardGameRepository.findByBggId(dto.getBoardgame().getBggId())
            .orElseGet(() -> {
                try {
                    return bggService.importGame(dto.getBoardgame().getBggId());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to import board game from BGG", e);
                }
            });

        // Luo GameSession
        GameSession game = new GameSession();
        game.setBoardgame(boardGame);
        game.setCreator(currentUser);
        game.setDescription(dto.getDescription());
        game.setEstimatedDuration(dto.getEstimatedDuration());
        game.setStartTime(dto.getStartTime());
        game.setMinPlayers(dto.getMinPlayers());
        game.setMaxPlayers(dto.getMaxPlayers());

        // Hae Event
        Event event = eventRepository.findById(dto.getEventId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        game.setEvent(event);

        gameSessionRepository.save(game);

        return new GameSessionDTO(game);
    }

    //UPDATE game (vain sallitut kentät)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER','MODERATOR','ADMIN')")
    public GameSessionDTO updateGame(@PathVariable Long id,
                                    @RequestBody GameSessionUpdateDTO dto) {

        GameSession existing = gameSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid game id"));

        if (dto.getStartTime() == null) {
            throw new IllegalArgumentException("Start time must not be null");
        }

        existing.setStartTime(dto.getStartTime());
        existing.setDescription(dto.getDescription());
        existing.setMinPlayers(dto.getMinPlayers());
        existing.setMaxPlayers(dto.getMaxPlayers());

        gameSessionRepository.save(existing);

        return new GameSessionDTO(existing);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MODERATOR','ADMIN')")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {

        GameSession game = gameSessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        gameSessionRepository.delete(game);

        return ResponseEntity.noContent().build(); // Palauttaa HTTP 204 No Content
    }

    //JOIN game
    @PostMapping("/{id}/join")
    public GameSession joinGame(@PathVariable Long id,
                               @AuthenticationPrincipal AppUser currentUser) {

        GameSession session = gameSessionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid game session id"));

        boolean alreadyJoined = session.getParticipants().stream()
                .anyMatch(u -> u.getId().equals(currentUser.getId()));

        if (!alreadyJoined) {
            session.getParticipants().add(currentUser);
            session.setParticipantCount(session.getParticipants().size());
            gameSessionRepository.save(session);
        }

        return session;
    }

    //LEAVE game
    @PostMapping("/{id}/leave")
    public GameSession leaveGame(@PathVariable Long id,
                                @AuthenticationPrincipal AppUser currentUser) {

        GameSession session = gameSessionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid game session id"));

        session.getParticipants().removeIf(u -> u.getId().equals(currentUser.getId()));
        session.setParticipantCount(session.getParticipants().size());

        return gameSessionRepository.save(session);
    }
}