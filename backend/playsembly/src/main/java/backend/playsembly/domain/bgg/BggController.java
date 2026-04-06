package backend.playsembly.domain.bgg;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import backend.playsembly.domain.*;
import backend.playsembly.dto.WantListItemDTO;

@RestController
@RequestMapping("/api/bgg")
public class BggController {

    private final BggService bggService;
    private final BoardGameRepository boardGameRepository;
    private final WantListRepository wantListRepository;
    private final BringListItemRepository bringListItemRepository;
    private final AppUserRepository appUserRepository;
    private final EventRepository eventRepository;

    public BggController(BggService bggService,
                         BoardGameRepository boardGameRepository,
                         WantListRepository wantListRepository,
                         WantListItemRepository wantListItemRepository,
                         BringListItemRepository bringListItemRepository,
                         AppUserRepository appUserRepository,
                         EventRepository eventRepository) {
        this.bggService = bggService;
        this.boardGameRepository = boardGameRepository;
        this.wantListRepository = wantListRepository;
        this.bringListItemRepository = bringListItemRepository;
        this.appUserRepository = appUserRepository;
        this.eventRepository = eventRepository;
    }

    /* Pelihaku BGG:stä käyttäen BGG:n omaa XML -API:a.
       Kaikki kirjautuneet käyttäjät saavat tehdä hakuja tarvittaessa. */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public List<SearchResultDTO> search(@RequestParam String query) throws Exception {
        return bggService.searchGames(query);
    }

    /** Lisää peli Want-listalle tiettyyn tapahtumaan.
        Avoin kaikille kirjautuneille käyttäjille.     */
@PostMapping("/want/{bggId}")
@PreAuthorize("isAuthenticated()")
public WantListItemDTO addToWantList(
        @PathVariable Long bggId,
        @RequestParam Long eventId,
        @RequestParam(required = false) String username,
        @AuthenticationPrincipal AppUser currentUser) {

    AppUser user = currentUser != null ? currentUser :
                   appUserRepository.findByUsername(username);
    if (user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

    Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

    BoardGame game;
    Optional<BoardGame> optional = boardGameRepository.findByBggId(bggId);

    if (optional.isPresent()) {
        game = optional.get();
    } else {
        try {
            game = bggService.importGame(bggId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to import game from BGG", e);
        }
    }

    // Hae tai luo WantList tapahtumalle
    WantList wantList = wantListRepository.findByEvent(event)
            .orElseGet(() -> {
                WantList wl = new WantList();
                wl.setEvent(event);
                return wantListRepository.save(wl);
            });

    // Tarkista ettei peli ole jo listalla
    boolean exists = wantList.getItems().stream()
            .anyMatch(i -> i.getBoardgame().getBggId().equals(bggId)
                        && i.getUser().getId().equals(user.getId()));
    if (exists) throw new RuntimeException("Game already in want list");

    WantListItem item = new WantListItem();
    item.setUser(user);
    item.setBoardgame(game);
    item.setAddedAt(LocalDateTime.now());
    item.setWantList(wantList);  // <- kriittinen

    wantList.getItems().add(item);  // <- lisää item wantListiin
    wantListRepository.save(wantList);  // <- tallenna koko lista

    return new WantListItemDTO(game.getBggId(), game.getName(), game.getImageUrl(), user.getUsername());
}

    /** Lisää peli Bring-listalle tiettyyn tapahtumaan.
        Avoin kaikille kirjautuneille käyttäjille.    */
    @PostMapping("/bring/{bggId}")
    @PreAuthorize("isAuthenticated()")
    public BringListItem addToBring(@PathVariable Long bggId,
                                    @RequestParam Long eventId,
                                    @AuthenticationPrincipal AppUser currentUser) throws Exception {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        BoardGame boardGame;
        Optional<BoardGame> optionalGame = boardGameRepository.findByBggId(bggId);

        if (optionalGame.isPresent()) {
            boardGame = optionalGame.get();
        } else {
            try {
                boardGame = bggService.importGame(bggId);
            } catch (Exception e) {
                throw new RuntimeException("Failed to import game from BGG", e);
            }
        }

        Optional<BringListItem> existing = bringListItemRepository.findByEventAndUserAndBoardgame(event, currentUser, boardGame);
        if (existing.isPresent()) return existing.get();

        BringListItem bringItem = new BringListItem();
        bringItem.setEvent(event);
        bringItem.setUser(currentUser);
        bringItem.setBoardgame(boardGame);
        bringItem.setAddedAt(LocalDateTime.now());

        return bringListItemRepository.save(bringItem);
    }

    /* Poista peli Bring-listalta.
        Avoin kaikille kirjautuneille käyttäjille. Toki tarkistetaan että käyttäjä on itse sanonut tuovansa poistettavan pelin. */
    @PostMapping("/bring/remove/{bggId}")
    @PreAuthorize("isAuthenticated()")
    public void removeFromBring(@PathVariable Long bggId,
                                @RequestParam Long eventId,
                                @AuthenticationPrincipal AppUser currentUser) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        BoardGame boardGame = boardGameRepository.findByBggId(bggId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with BGG ID: " + bggId));

        //Tarkistaa että tapahtumassa löytyy käyttäjän lisäämä lautapeli bringlistalla ja poistaa sen jos näin on.
        bringListItemRepository.findByEventAndUserAndBoardgame(event, currentUser, boardGame)
                .ifPresent(bringListItemRepository::delete);
    }
}