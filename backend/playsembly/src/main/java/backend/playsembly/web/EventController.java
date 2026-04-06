package backend.playsembly.web;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import backend.playsembly.domain.AppUser;
import backend.playsembly.domain.AppUserRepository;
import backend.playsembly.domain.BoardGameRepository;
import backend.playsembly.domain.BringList;
import backend.playsembly.domain.BringListItem;
import backend.playsembly.domain.BringListRepository;
import backend.playsembly.domain.Event;
import backend.playsembly.domain.EventRepository;
import backend.playsembly.domain.GameSession;
import backend.playsembly.domain.GameSessionRepository;
import backend.playsembly.domain.WantList;
import backend.playsembly.domain.WantListItem;
import backend.playsembly.domain.WantListItemRepository;
import backend.playsembly.domain.WantListRepository;
import backend.playsembly.domain.bgg.BggService;
import backend.playsembly.domain.bgg.BoardGame;
import jakarta.validation.Valid;

@Controller
public class EventController {
    
    private final WantListItemRepository wantListItemRepository;
    private final EventRepository eventRepository;
    private final GameSessionRepository gameSessionRepository;
    private final WantListRepository wantListRepository;
    private final BringListRepository bringListRepository;
    private final BoardGameRepository boardGameRepository;
    private final BggService bggService;

    public EventController(EventRepository eventRepository, GameSessionRepository gameSessionRepository, WantListRepository wantListRepository, WantListItemRepository wantListItemRepository, BringListRepository bringListRepository, BoardGameRepository boardGameRepository, BggService bggService, AppUserRepository appUserRepository) {
        this.eventRepository = eventRepository;
        this.gameSessionRepository=gameSessionRepository;
        this.wantListRepository=wantListRepository;
        this.wantListItemRepository = wantListItemRepository;
        this.bringListRepository=bringListRepository;
        this.boardGameRepository=boardGameRepository;
        this.bggService=bggService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/events")
    public String home(Model model) {
        model.addAttribute("events", eventRepository.findAll());
        return "home";
    }

    @GetMapping("/event/new")
    public String showCreateEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "createEvent";
    }

    //Eventin luonti vain Mod tai korkeampi rooli
    @PostMapping("/event")
    @PreAuthorize("hasAnyAuthority('MODERATOR','ADMIN')")
    public String createEvent(@ModelAttribute Event event) {
        eventRepository.save(event);
        return "redirect:/events";
    }

    /* Kuka tahansa kirjautunut käyttäjä voi nähdä tapahtuman sisällön.
       Jatkokehitysideana voisi olla esimerkiksi kutsukoodi jolla tapahtumaan voisi liittyä. */
    @GetMapping("/event/{id}")
    @PreAuthorize("isAuthenticated()")
    public String showEvent(@PathVariable Long id, Model model, @AuthenticationPrincipal AppUser currentUser) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + id));

        List<GameSession> games = gameSessionRepository.findByEvent(event);
        games.sort(Comparator.comparing(GameSession::getStartTime));

        // Luo Map, joka kertoo kenellä on edit-oikeus
        Map<Long, Boolean> gameEditMap = new HashMap<>();
        for (GameSession g : games) {
            boolean canEdit = g.getCreator().getId().equals(currentUser.getId()) 
                            || currentUser.getRole().equals("MODERATOR") 
                            || currentUser.getRole().equals("ADMIN");
            gameEditMap.put(g.getId(), canEdit);
        }

        model.addAttribute("event", event);
        model.addAttribute("games", games);
        model.addAttribute("gameEditMap", gameEditMap);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails current = (UserDetails) auth.getPrincipal();
        model.addAttribute("currentUser", current);

        List<WantListItem> wantList = wantListRepository.findByEvent(event)
        .map(WantList::getItems)
        .orElse(List.of());

        List<BringListItem> bringList = bringListRepository.findByEvent(event)
                .map(BringList::getItems)
                .orElse(List.of());

        model.addAttribute("wantList", wantList);
        model.addAttribute("bringList", bringList);

        return "showEvent";
    }
    
    //Vain Mod tai korkeampi rooli saa muokata tapahtumia.
    @GetMapping("/event/edit/{id}")
    @PreAuthorize("hasAnyAuthority('MODERATOR', 'ADMIN')")
    public String showEditEventForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Event> eventOpt = eventRepository.findById(id);

        if (eventOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Event not found!");
            return "redirect:/events";
        }

        Event event = eventOpt.get();
        model.addAttribute("event", event);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        if (event.getStartTime() != null)
            model.addAttribute("startTimeString", event.getStartTime().format(dtf));
        if (event.getEndTime() != null)
            model.addAttribute("endTimeString", event.getEndTime().format(dtf));

        // Nykyhetki min-arvoksi
        model.addAttribute("minDateTime", LocalDateTime.now().format(dtf));

        return "createEvent";
    }

    //Koska vain Mod tai korkeampi saa luoda tapahtuman, niin luonnollisesti sama pätee tapahtuman tallennukseen.
    @PostMapping("/event/save")
    @PreAuthorize("hasAnyAuthority('MODERATOR', 'ADMIN')")
    public String saveEvent(@ModelAttribute Event event, RedirectAttributes redirectAttributes) {

        //Käyttäjäsyötteen validointi, tapahtumaa ei voi luoda menneisyyteen.
        if (event.getStartTime() != null && event.getStartTime().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Start time cannot be in the past!");
            return event.getId() != null ? "redirect:/event/edit/" + event.getId() : "redirect:/event/new";
        }

        redirectAttributes.addFlashAttribute("successMessage", 
            event.getId() != null ? "Event päivitetty onnistuneesti" : "Event luotu onnistuneesti");

        //tallennetaan tapahtuma
        eventRepository.save(event);

        return "redirect:/events";
    }
    
    //Kuka tahansa kirjautunut käyttäjä voi avata pelinluontinäkymän tapahtuman sisältä.
    @GetMapping("/event/{id}/game/new")
    @PreAuthorize("isAuthenticated()")
    public String showCreateGameForm(@PathVariable Long id, Model model) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event id"));

        GameSession game = new GameSession();
        game.setEvent(event);

        model.addAttribute("game", game);
        model.addAttribute("event", event);

        //Passataan modelille tapahtuman voimassaoloajat ja rajataan pelin luonti näiden sisään thymeleafissa.
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        if (event.getStartTime() != null) {
            model.addAttribute("eventStart", event.getStartTime().format(dtf));
        }
        if (event.getEndTime() != null) {
            model.addAttribute("eventEnd", event.getEndTime().format(dtf));
        }

            return "createGame";
        }
    
        //Kuka tahansa kirjautunut käyttäjä voi tallettaa luomansa pelin tapahtumaan.
        @PostMapping("/event/{id}/game")
        @PreAuthorize("isAuthenticated()")
        public String createGame(@PathVariable Long id,
                                @Valid @ModelAttribute("game") GameSession formGame,
                                BindingResult result,
                                Model model,
                                @AuthenticationPrincipal AppUser currentAppUser) throws Exception {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event id"));


        // 🔹 Luo uusi GameSession
        GameSession newGame = new GameSession();

        // 🔹 Hae boardgame tietokannasta tai importoi BGG:stä
        BoardGame formBoardGame = formGame.getBoardgame();
        BoardGame boardGame;

        Optional<BoardGame> optional = boardGameRepository.findByBggId(formBoardGame.getBggId());
        if (optional.isPresent()) {
            boardGame = optional.get();
        } else {
            // Käytetään BggService importGame -metodia
            boardGame = bggService.importGame(formBoardGame.getBggId());
        }

        // 🔹 Aseta BoardGame GameSessioniin
        newGame.setBoardgame(boardGame);

        // 🔹 Aseta muut GameSession-kentät
        newGame.setEvent(event);
        newGame.setStartTime(formGame.getStartTime());
        newGame.setMinPlayers(formGame.getMinPlayers());
        newGame.setMaxPlayers(formGame.getMaxPlayers());
        newGame.setEstimatedDuration(formGame.getEstimatedDuration());
        newGame.setDescription(formGame.getDescription());
        newGame.setCreator(currentAppUser);

        if (formGame.getMinPlayers() > formGame.getMaxPlayers()) {
            result.rejectValue("minPlayers", "error.game", "Min players cannot exceed max players");
        }

        if (result.hasErrors()) {
            model.addAttribute("event", event);
            model.addAttribute("game", formGame); //valittuna ollut peli
            return "createGame"; // palaat lomakkeelle, tallennusta ei tapahdu
        }
                
        // 🔹 Tallenna GameSession
        gameSessionRepository.save(newGame);

        return "redirect:/event/" + id;
    }

    //ADMIN voi poistaa minkä tahansa tapahtuman.
    @PostMapping("event/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Tarkistetaan, löytyykö event
        eventRepository.findById(id).ifPresentOrElse(event -> {
            eventRepository.delete(event);
        }, () -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Event ei löytynyt");
        });

        //Välitetään viesti käyttäjälle
        redirectAttributes.addFlashAttribute("successMessage", "Event poistettu onnistuneesti");
        
        // Paluu listausnäkymään
        return "redirect:/events"; // muuta näkymän polku tarpeen mukaan
    }

    //Kuka tahansa kirjautunut käyttäjä voi lisätä pelin tapahtumakohtaiselle toivomuslistalle.
    @PostMapping("/event/{eventId}/want/add/{bggId}")
    @PreAuthorize("isAuthenticated()")
    public String addToWantList(
            @PathVariable Long eventId,
            @PathVariable Long bggId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + eventId));

        BoardGame game;
        try {
            game = bggService.importGame(bggId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to import game from BGG", e);
        }

        // Hae eventin WantList, luo jos ei ole
        WantList wantList = wantListRepository.findByEvent(event)
                .orElseGet(() -> {
                    WantList wl = new WantList();
                    wl.setEvent(event);
                    return wantListRepository.save(wl);
                });

        // Tarkista, onko käyttäjä jo lisännyt pelin
        boolean exists = wantList.getItems().stream()
                .anyMatch(item -> item.getBoardgame().getBggId().equals(bggId)
                        && item.getUser().getId().equals(currentUser.getId()));

        if (!exists) {
            WantListItem item = new WantListItem();
            item.setWantList(wantList);
            item.setBoardgame(game);
            item.setUser(currentUser);
            item.setAddedAt(LocalDateTime.now());

            wantListItemRepository.save(item); // <-- Tallenna item suoraan
        }

        return "redirect:/event/" + eventId;
    }

    //Kuka tahansa kirjautunut käyttäjä voi tapahtumakohtaiselta wantlistilta valita pelin ja ilmoittaa tuovansa sen eli siirtää sen bringlistille.
    @PostMapping("/event/{eventId}/bring/add/{bggId}")
    @PreAuthorize("isAuthenticated()")
    public String moveToBringList(
            @PathVariable Long eventId,
            @PathVariable Long bggId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + eventId));

        BoardGame game;
        try {
            game = bggService.importGame(bggId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to import game from BGG", e);
        }

        // Hae eventin WantList ja poista item käyttäjältä jos löytyy
        wantListRepository.findByEvent(event).ifPresent(wl -> {
            wl.getItems().removeIf(item -> item.getBoardgame().getBggId().equals(bggId)
                    && item.getUser().getId().equals(currentUser.getId()));
            wantListRepository.save(wl);
        });

        // Hae eventin BringList, luo jos ei ole
        BringList bringList = bringListRepository.findByEvent(event)
                .orElseGet(() -> {
                    BringList bl = new BringList();
                    bl.setEvent(event);
                    return bringListRepository.save(bl);
                });

        // Tarkista, onko käyttäjä jo lisännyt pelin BringListalle
        boolean exists = bringList.getItems().stream()
                .anyMatch(item -> item.getBoardgame().getBggId().equals(bggId)
                        && item.getUser().getId().equals(currentUser.getId()));

        if (!exists) {
            BringListItem item = new BringListItem();
            item.setBringList(bringList);
            item.setBoardgame(game);
            item.setUser(currentUser);
            item.setAddedAt(LocalDateTime.now());

            bringList.getItems().add(item);
            bringListRepository.save(bringList);
        }

        return "redirect:/event/" + eventId;
    }

    @PostMapping("/event/{eventId}/want/remove/{bggId}")
    @PreAuthorize("@gameSecurity.isWantItemOwnerOrAdmin(#eventId, #bggId, principal)")
    public String removeFromWantList(
            @PathVariable Long eventId,
            @PathVariable Long bggId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event id"));

        WantListItem wantListItem = wantListRepository.findByEvent(event)
                .flatMap(wl -> wl.getItems().stream()
                        .filter(item -> item.getBoardgame().getBggId().equals(bggId))
                        .findFirst())
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        wantListItem.getWantList().getItems().remove(wantListItem);
        wantListRepository.save(wantListItem.getWantList());

        return "redirect:/event/" + eventId;
    }

    //Kuka tahansa käyttäjä voi perua ilmoituksensa pelin tuomisesta.
    @PostMapping("/event/{eventId}/bring/back/{bggId}")
    @PreAuthorize("isAuthenticated()")
    public String moveBackToWant(
            @PathVariable Long eventId,
            @PathVariable Long bggId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + eventId));

        BoardGame game;
        try {
            game = bggService.importGame(bggId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to import game from BGG", e);
        }

        // Poista käyttäjän item BringListalta
        bringListRepository.findByEvent(event).ifPresent(bl -> {
            bl.getItems().removeIf(item -> item.getBoardgame().getBggId().equals(bggId)
                    && item.getUser().getId().equals(currentUser.getId()));
            bringListRepository.save(bl);
        });

        // Lisää item takaisin WantListalle
        WantList wantList = wantListRepository.findByEvent(event)
                .orElseGet(() -> {
                    WantList wl = new WantList();
                    wl.setEvent(event);
                    return wantListRepository.save(wl);
                });

        boolean exists = wantList.getItems().stream()
                .anyMatch(item -> item.getBoardgame().getBggId().equals(bggId)
                        && item.getUser().getId().equals(currentUser.getId()));

        if (!exists) {
            WantListItem item = new WantListItem();
            item.setWantList(wantList);
            item.setBoardgame(game);
            item.setUser(currentUser);
            item.setAddedAt(LocalDateTime.now());

            wantList.getItems().add(item);
            wantListRepository.save(wantList);
        }

        return "redirect:/event/" + eventId;
    }

}
