package backend.playsembly.dto;

import java.time.LocalDateTime;
import java.util.List;

//pakollinen DTO jotta vältytään JSON infinite nesting ongelmalta pelin lisäämisessä wantlistalle
public class EventDTO {
    private Long id;
    private String name;
    private String city;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<GameSessionDTO> sessions;
    
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
    public List<GameSessionDTO> getSessions() {
        return sessions;
    }
    public void setSessions(List<GameSessionDTO> sessions) {
        this.sessions = sessions;
    }

    
}
