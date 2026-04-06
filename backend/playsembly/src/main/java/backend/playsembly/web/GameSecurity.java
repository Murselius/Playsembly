package backend.playsembly.web;

import org.springframework.stereotype.Component;

import backend.playsembly.domain.AppUser;
import backend.playsembly.domain.Event;
import backend.playsembly.domain.EventRepository;
import backend.playsembly.domain.GameSessionRepository;
import backend.playsembly.domain.WantListItem;
import backend.playsembly.domain.WantListRepository;


/* Apuluokka jossa määritetään logiikka tarkistukselle onko nykyinen käyttäjä pelin luoja tai onko hänellä moderator oikeudet.
  Palauttaa booleanin. */
@Component("gameSecurity")
public class GameSecurity {

    private final EventRepository eventRepository;
    private final GameSessionRepository gameSessionRepository;
    private final WantListRepository wantListRepository;

    public GameSecurity(GameSessionRepository gameSessionRepository, WantListRepository wantListRepository, EventRepository eventRepository) {
        this.gameSessionRepository = gameSessionRepository;
        this.wantListRepository = wantListRepository;
        this.eventRepository = eventRepository;
    }

    // Pelisession luoja tai moderator tai admin
    public boolean isCreatorOrAdmin(Long gameId, AppUser principal) {
        return gameSessionRepository.findById(gameId)
            .map(game -> game.getCreator().getId().equals(principal.getId())
                        || principal.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("MODERATOR") 
                                        || auth.getAuthority().equals("ADMIN")))
            .orElse(false);
    }

    //Tarkistus onko pelin pyytäjä, moderator tai admin
    public boolean isWantItemOwnerOrAdmin(Long eventId, Long bggId, AppUser currentUser) {
        if (currentUser == null) return false;

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return false;

        WantListItem item = wantListRepository.findByEvent(event)
                .flatMap(wl -> wl.getItems().stream()
                        .filter(i -> i.getBoardgame().getBggId().equals(bggId))
                        .findFirst())
                .orElse(null);

        if (item == null) return false; // tärkeä null-tarkistus

        boolean isOwner = item.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().equals("ROLE_ADMIN");

        return isOwner || isAdmin;
    }


    public boolean isWantItemOwnerOrAdmin(WantListItem item, AppUser currentUser) {
        if (item == null || currentUser == null) return false;

        boolean isOwner = item.getUser() != null && item.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole().equals("ROLE_ADMIN");

        return isOwner || isAdmin;
    }
}