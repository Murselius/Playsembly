package backend.playsembly.domain;

import backend.playsembly.domain.bgg.BoardGame;
import jakarta.persistence.*;

@Entity
public class BringListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Event event;

    @ManyToOne
    private BoardGame boardgame;

    @ManyToOne
    private AppUser user;

    public BringListItem() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public BoardGame getBoardgame() {
        return boardgame;
    }

    public void setBoardgame(BoardGame boardgame) {
        this.boardgame = boardgame;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "BringListItem [id=" + id + ", event=" + event + ", boardgame=" + boardgame + ", user=" + user + "]";
    }

    
}