package backend.playsembly.dto;

import java.time.LocalDateTime;

public class GameSessionUpdateDTO {
    private String description;
    private LocalDateTime startTime;
    private int minPlayers;
    private int maxPlayers;

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public int getMinPlayers() {
        return minPlayers;
    }
    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }
    public int getMaxPlayers() {
        return maxPlayers;
    }
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    public GameSessionUpdateDTO(String description, LocalDateTime startTime, int minPlayers, int maxPlayers) {
        this.description = description;
        this.startTime = startTime;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public GameSessionUpdateDTO() {
        
    }
    
}