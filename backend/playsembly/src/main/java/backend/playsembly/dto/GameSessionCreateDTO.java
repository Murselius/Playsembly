package backend.playsembly.dto;

import java.time.LocalDateTime;

public class GameSessionCreateDTO {

    private BoardGameIdDTO boardgame;
    private String description;
    private int estimatedDuration;
    private int minPlayers;
    private int maxPlayers;
    private LocalDateTime startTime;
    private Long eventId; //Tapahtuma johon peli liittyy

    // getters & setters
    public BoardGameIdDTO getBoardgame() { return boardgame; }
    public void setBoardgame(BoardGameIdDTO boardgame) { this.boardgame = boardgame; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(int estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public int getMinPlayers() { return minPlayers; }
    public void setMinPlayers(int minPlayers) { this.minPlayers = minPlayers; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    //sisäluokka pelin tunnistusta varten
    public static class BoardGameIdDTO {
        private Long bggId;
        public Long getBggId() { return bggId; }
        public void setBggId(Long bggId) { this.bggId = bggId; }
    }
}