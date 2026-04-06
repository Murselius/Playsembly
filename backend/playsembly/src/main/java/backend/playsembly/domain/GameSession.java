package backend.playsembly.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import backend.playsembly.domain.bgg.BoardGame;

@Entity
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @Min(value = 1, message = "Minimum players must be at least 1")
    @NotNull(message = "Min players is required")
    private int minPlayers;

    @Min(value = 1, message = "Maximum players must be at least 1")
    @NotNull(message = "Max players is required")
    private int maxPlayers;

    @Min(value = 1, message = "Estimated duration must be at least 1 minute")
    @NotNull(message = "Estimated duration is required")
    private Integer estimatedDuration;

    @Column(length = 1000)
    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    private int participantCount;

    private boolean rulesExplanationNeeded;

    // Relaatiot
    @ManyToOne
    @JsonIgnore
    private Event event;

    @ManyToOne
    private BoardGame boardgame;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id")
    private AppUser creator;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "game_session_participants",
        joinColumns = @JoinColumn(name = "game_session_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<AppUser> participants = new HashSet<>();

    public GameSession() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public int getMinPlayers() { return minPlayers; }
    public void setMinPlayers(int minPlayers) { this.minPlayers = minPlayers; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public Integer getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getParticipantCount() { return participantCount; }
    public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }

    public boolean isRulesExplanationNeeded() { return rulesExplanationNeeded; }
    public void setRulesExplanationNeeded(boolean rulesExplanationNeeded) { this.rulesExplanationNeeded = rulesExplanationNeeded; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public BoardGame getBoardgame() { return boardgame; }
    public void setBoardgame(BoardGame boardgame) { this.boardgame = boardgame; }

    public AppUser getCreator() { return creator; }
    public void setCreator(AppUser creator) { this.creator = creator; }

    public Set<AppUser> getParticipants() { return participants; }
    public void setParticipants(Set<AppUser> participants) { this.participants = participants; }

    @Transient
    public Set<Long> getParticipantIds() {
        return participants.stream()
                        .map(AppUser::getId)
                        .collect(Collectors.toSet());
    }

    //testien validointiin
    @PrePersist
    @PreUpdate
    private void validateLogic() {
        if (minPlayers > maxPlayers) {
            throw new IllegalArgumentException("Min players cannot be greater than max players");
        }
        if (startTime != null && startTime.isAfter(LocalDateTime.now().plusYears(5))) {
            throw new IllegalArgumentException("Start time cannot be more than 5 years in the future");
        }
    }

    //Luonnin aikaiseen validointiin
    @AssertTrue(message = "Min players cannot be greater than max players")
    public boolean isValidPlayerCount() {
        return minPlayers <= maxPlayers;
    }

    @AssertTrue(message = "Start time cannot be more than 5 years in the future")
    public boolean isValidStartTime() {
        if (startTime == null) return true;
        return !startTime.isAfter(LocalDateTime.now().plusYears(5));
    }

    @Override
    public String toString() {
        return "GameSession [id=" + id + ", startTime=" + startTime + ", minPlayers=" + minPlayers +
                ", maxPlayers=" + maxPlayers + ", estimatedDuration=" + estimatedDuration +
                ", description=" + description + ", participantCount=" + participantCount +
                ", rulesExplanationNeeded=" + rulesExplanationNeeded + ", event=" + event +
                ", boardgame=" + boardgame + ", creator=" + creator + "]";
    }
}