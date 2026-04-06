package backend.playsembly.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class BringList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private AppUser user;

    @OneToOne
    private Event event;  // Tapahtuma, johon listaus kuuluu

    @OneToMany(mappedBy = "bringList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BringListItem> items = new ArrayList<>();

    private LocalDateTime addedAt;

    // Getterit ja setterit
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public List<BringListItem> getItems() { return items; }
    public void setItems(List<BringListItem> items) { this.items = items; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
    
    public Event getEvent() { return event;}
    public void setEvent(Event event) { this.event = event; }

    
}