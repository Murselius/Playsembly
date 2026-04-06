package backend.playsembly.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import backend.playsembly.domain.bgg.BoardGame;

@Entity
public class BringListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Event event;

    @ManyToOne
    private AppUser user;

    @ManyToOne
    private BringList bringList;

    @ManyToOne
    private BoardGame boardgame;

    private LocalDateTime addedAt;

    // Getterit ja setterit
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public BoardGame getBoardgame() { return boardgame; }
    public void setBoardgame(BoardGame boardgame) { this.boardgame = boardgame; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    public BringList getBringList() { return bringList; }
    public void setBringList(BringList bringList) { this.bringList = bringList; }

    
}