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

import backend.playsembly.domain.AppUser;
import backend.playsembly.domain.AppUserRepository;
import backend.playsembly.domain.BoardGameRepository;
import backend.playsembly.domain.BringListRepository;
import backend.playsembly.domain.Event;
import backend.playsembly.domain.EventRepository;
import backend.playsembly.domain.GameSession;
import backend.playsembly.domain.GameSessionRepository;
import backend.playsembly.domain.WantListRepository;
import backend.playsembly.domain.bgg.BggService;
import backend.playsembly.domain.bgg.BoardGame;
import jakarta.validation.Valid;

@Controller
public class EventController {
    
    private final AppUserRepository appUserRepository;
    private final EventRepository eventRepository;
    private final GameSessionRepository gameSessionRepository;
    private final WantListRepository wantListRepository;
    private final BringListRepository bringListRepository;
    private final BoardGameRepository boardGameRepository;
    private final BggService bggService;

    public EventController(EventRepository eventRepository, GameSessionRepository gameSessionRepository, WantListRepository wantListRepository, BringListRepository bringListRepository, BoardGameRepository boardGameRepository, BggService bggService, AppUserRepository appUserRepository) {
        this.eventRepository = eventRepository;
        this.gameSessionRepository=gameSessionRepository;
        this.wantListRepository=wantListRepository;
        this.bringListRepository=bringListRepository;
        this.boardGameRepository=boardGameRepository;
        this.bggService=bggService;
        this.appUserRepository = appUserRepository;
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

    @PostMapping("/event")
    public String createEvent(@ModelAttribute Event event) {
        eventRepository.save(event);
        return "redirect:/events";
    }

    @GetMapping("/event/{id}")
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

        return "showEvent";
    }
    
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

    @PostMapping("/event/save")
    @PreAuthorize("hasAnyAuthority('MODERATOR', 'ADMIN')")
    public String saveEvent(@ModelAttribute Event event, RedirectAttributes redirectAttributes) {

        // Jos event.id on null → luodaan uusi
        // Jos event.id on olemassa → päivitetään
        eventRepository.save(event);

        //Käyttäjäsyötteen validointi, tapahtumaa ei voi luoda menneisyyteen.
        if (event.getStartTime() != null && event.getStartTime().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Start time cannot be in the past!");
            return event.getId() != null ? "redirect:/event/edit/" + event.getId() : "redirect:/event/new";
        }

        redirectAttributes.addFlashAttribute("successMessage", 
            event.getId() != null ? "Event päivitetty onnistuneesti" : "Event luotu onnistuneesti");

        return "redirect:/events";
    }
    
    @GetMapping("/event/{id}/game/new")
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
    
        @PostMapping("/event/{id}/game")
        public String createGame(@PathVariable Long id,
                                @Valid @ModelAttribute("game") GameSession formGame,
                                BindingResult result,
                                Model model,
                                @AuthenticationPrincipal AppUser currentAppUser) throws Exception {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event id"));

        if (result.hasErrors()) {
            model.addAttribute("event", event);
            return "createGame";
        }

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
        
        // 🔹 Tallenna GameSession
        gameSessionRepository.save(newGame);

        return "redirect:/event/" + id;
    }

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

}
