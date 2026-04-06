package backend.playsembly.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class WantList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Event event;  // Tapahtuma, johon listaus kuuluu

    @OneToMany(mappedBy = "wantList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WantListItem> items = new ArrayList<>();

    // Getterit / setterit
    public Long getId() { return id; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public List<WantListItem> getItems() { return items; }
    public void setItems(List<WantListItem> items) { this.items = items; }
}