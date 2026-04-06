package backend.playsembly.web;

import java.time.format.DateTimeFormatter;
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
import backend.playsembly.domain.Event;
import backend.playsembly.domain.GameSession;
import backend.playsembly.domain.GameSessionRepository;
import jakarta.validation.Valid;

@Controller
public class GameSessionController {

    private final GameSessionRepository gameSessionRepository;

    public GameSessionController(GameSessionRepository gameSessionRepository) {
        this.gameSessionRepository = gameSessionRepository;
    }

    @GetMapping("/game/{id}")
    public String showGameSession(@PathVariable Long id,
                                @AuthenticationPrincipal AppUser currentUser,
                                Model model) {

        GameSession game = gameSessionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid game session id"));

        model.addAttribute("game", game);
        

        // userJoined ei välttämätön, koska Thymeleaf käyttää participantIds nyt
        return "showGame";
    }

    //Haetaan olemassa olevan pelisession tiedot ja palataan näiden kanssa luonti sivulle.
    @GetMapping("/game/edit/{id}")
    @PreAuthorize("@gameSecurity.isCreatorOrAdmin(#id, principal)")
    public String showEditGameForm(@PathVariable Long id, Model model) {
        GameSession game = gameSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid game id"));

        model.addAttribute("game", game);
        model.addAttribute("event", game.getEvent());

        if (game.getStartTime() == null) {
            throw new IllegalArgumentException("Start time must not be null");
        }

        // Muotoillaan päivämäärät input-lomakkeelle
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        Event event = game.getEvent();
        if (event.getStartTime() != null) {
            model.addAttribute("eventStart", event.getStartTime().format(dtf));
        }
        if (event.getEndTime() != null) {
            model.addAttribute("eventEnd", event.getEndTime().format(dtf));
        }

        return "createGame"; // käytetään samaa lomaketta kuin uuden luonti
    }

    //Muokatun pelin tallennus säilyttäen olemassa oleva osallistujalista. Moderator oikeuksinen tai pelin luonut käyttäjä.
    @PostMapping("/game/update/{id}")
    @PreAuthorize("@gameSecurity.isCreatorOrAdmin(#id, principal)")
    public String updateGame(@PathVariable Long id,
                            @Valid @ModelAttribute GameSession updatedGame,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("game", updatedGame);
            model.addAttribute("event", updatedGame.getEvent());
            return "createGame"; // takaisin lomakkeelle
        }

        GameSession existing = gameSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid game id"));

        existing.setStartTime(updatedGame.getStartTime());
        existing.setDescription(updatedGame.getDescription());
        existing.setMinPlayers(updatedGame.getMinPlayers());
        existing.setMaxPlayers(updatedGame.getMaxPlayers());

        gameSessionRepository.save(existing);

        return "redirect:/event/" + existing.getEvent().getId();
    }

    //Kirjautunut käyttäjä voi liittyä peliin
    @PostMapping("/game/{id}/join")
    @PreAuthorize("isAuthenticated()")
    public String joinGame(@PathVariable Long id,
                        @AuthenticationPrincipal AppUser currentUser,
                        RedirectAttributes redirectAttributes) {

        GameSession session = gameSessionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid game session id"));

        // Tarkista, ettei käyttäjä ole jo osallistuja
        if (session.getParticipants().contains(currentUser)) {
            redirectAttributes.addFlashAttribute("error", "You have already joined this game!");
            return "redirect:/game/" + id;
        }

        session.getParticipants().add(currentUser);
        session.setParticipantCount(session.getParticipants().size());
        gameSessionRepository.save(session);

        redirectAttributes.addFlashAttribute("message", "You have joined the game!");
        return "redirect:/game/" + id;
    }

    //Kirjautunut käyttäjä voi poistua pelistä
    @PostMapping("/game/{id}/unjoin")
    @PreAuthorize("isAuthenticated()")
    public String leaveGame(@PathVariable Long id,
                            @AuthenticationPrincipal AppUser currentUser,
                            RedirectAttributes redirectAttributes) {

        GameSession session = gameSessionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid game session id"));

        //Tarkistetaan käyttäjän ID:n perusteella
        boolean isParticipant = session.getParticipants().stream()
                .anyMatch(u -> u.getId().equals(currentUser.getId()));

        if (!isParticipant) {
            redirectAttributes.addFlashAttribute("error", "You are not part of this game!");
            return "redirect:/game/" + id;
        }

        //Poistetaan käyttäjä ID:n perusteella
        session.getParticipants().removeIf(u -> u.getId().equals(currentUser.getId()));
        session.setParticipantCount(session.getParticipants().size());
        gameSessionRepository.save(session);

        redirectAttributes.addFlashAttribute("message", "You have left the game.");
        return "redirect:/game/" + id;
    }

    //Pelin luoja tai Mod tai suurempi arvoinen käyttäjä voi poistaa pelin
    @PostMapping("/game/delete/{id}")
    @PreAuthorize("@gameSecurity.isCreatorOrAdmin(#id, principal)")
    public String deleteGame(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        GameSession session = gameSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid game id"));

        Long eventId = session.getEvent().getId();
        gameSessionRepository.delete(session);

        redirectAttributes.addFlashAttribute("message", "Game session deleted successfully!");
        return "redirect:/event/" + eventId;
    }
}
