package backend.playsembly.web;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import backend.playsembly.domain.*;
import backend.playsembly.domain.bgg.BggService;
import backend.playsembly.domain.bgg.BoardGame;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events")
public class RestEventController {

    private final EventRepository eventRepository;
    private final GameSessionRepository gameSessionRepository;
    private final WantListRepository wantListRepository;
    private final BoardGameRepository boardGameRepository;
    private final BggService bggService;

    public RestEventController(EventRepository eventRepository,
                               GameSessionRepository gameSessionRepository,
                               WantListRepository wantListRepository,
                               BringListRepository bringListRepository,
                               BoardGameRepository boardGameRepository,
                               BggService bggService) {
        this.eventRepository = eventRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.wantListRepository = wantListRepository;
        this.boardGameRepository = boardGameRepository;
        this.bggService = bggService;
    }

    //GET all events
    @GetMapping
    public Iterable<Event> getEvents() {
        return eventRepository.findAll();
    }

    //GET single event + sorted games
    @GetMapping("/{id}")
    public Event getEvent(@PathVariable Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event id"));

        List<GameSession> games = gameSessionRepository.findByEvent(event);
        games.sort(Comparator.comparing(GameSession::getStartTime));

        event.setGames(games); //vaatii että Event-entityssä on games-lista

        return event;
    }

    //CREATE a new event
    @PostMapping
    @PreAuthorize("hasAnyAuthority('MODERATOR', 'ADMIN')")
    public Event createEvent(@RequestBody Event event) {

        if (event.getStartTime() != null &&
            event.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time cannot be in the past");
        }

        return eventRepository.save(event);
    }

    //UPDATE an event
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MODERATOR', 'ADMIN')")
    public Event updateEvent(@PathVariable Long id, @RequestBody Event updated) {

        Event existing = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (updated.getStartTime() != null &&
            updated.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time cannot be in the past");
        }

        existing.setName(updated.getName());
        existing.setCity(updated.getCity());
        existing.setLocation(updated.getLocation());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());

        return eventRepository.save(existing);
    }

    //DELETE event
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteEvent(@PathVariable Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        eventRepository.delete(event);
    }

    //CREATE game session
    @PostMapping("/{id}/games")
    public GameSession createGame(@PathVariable Long id,
                                 @Valid @RequestBody GameSession formGame,
                                 @AuthenticationPrincipal AppUser currentUser) throws Exception {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event id"));

        GameSession newGame = new GameSession();

        //BoardGame lookup / import
        BoardGame formBoardGame = formGame.getBoardgame();

        BoardGame boardGame;

        Optional<BoardGame> optional = boardGameRepository.findByBggId(formBoardGame.getBggId());

        if (optional.isPresent()) {
            boardGame = optional.get();
        } else {
            try {
                boardGame = bggService.importGame(formBoardGame.getBggId());
            } catch (Exception e) {
                throw new RuntimeException("Failed to import game from BGG", e);
            }
        }

        newGame.setBoardgame(boardGame);

        newGame.setEvent(event);
        newGame.setStartTime(formGame.getStartTime());
        newGame.setMinPlayers(formGame.getMinPlayers());
        newGame.setMaxPlayers(formGame.getMaxPlayers());
        newGame.setEstimatedDuration(formGame.getEstimatedDuration());
        newGame.setDescription(formGame.getDescription());
        newGame.setCreator(currentUser);

        return gameSessionRepository.save(newGame);
    }

    //GET want list
    @GetMapping("/{id}/wantlist")
    public List<?> getWantList(@PathVariable Long id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid event id"));

        WantList wantList = wantListRepository.findByEvent(event)
        .orElse(new WantList()); // tyhjä lista, jos ei löydy

        return wantList.getItems();
    }

    //GET bring list
    @GetMapping("/{id}/bringlist")
    public List<?> getBringList(@PathVariable Long id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid event id"));

        WantList wantList = wantListRepository.findByEvent(event)
            .orElse(new WantList()); // tyhjä lista, jos ei löydy

        return wantList.getItems();
    }
}