package backend.playsembly.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private String city;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    //Relaatiot
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<GameSession> games;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<WantListItem> wantList;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<BringListItem> bringList;

    public Event() {}

    public Event(String name, String location, String city, LocalDateTime startTime, LocalDateTime endTime) {
        this.name = name;
        this.location = location;
        this.city = city;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public List<GameSession> getGames() {
        return games;
    }

    public void setGames(List<GameSession> games) {
        this.games = games;
    }

    public List<WantListItem> getWantList() {
        return wantList;
    }

    public void setWantList(List<WantListItem> wantList) {
        this.wantList = wantList;
    }

    public List<BringListItem> getBringList() {
        return bringList;
    }

    public void setBringList(List<BringListItem> bringList) {
        this.bringList = bringList;
    }

    @Override
    public String toString() {
        return "Event [id=" + id + ", name=" + name + ", city=" + city + ", location=" + location + ", startTime=" + startTime + ",endTime=" + endTime + "]";
    }
    
}