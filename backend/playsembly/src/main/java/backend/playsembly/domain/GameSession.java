package backend.playsembly.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import backend.playsembly.domain.bgg.BoardGame;

@Entity
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;

    @Min(1)
    @NotNull
    private int minPlayers;

    @Min(1)
    @NotNull
    private int maxPlayers;

    @Min(1)
    @NotNull
    private Integer estimatedDuration;

    @Column(length=1000)
    private String description;

    private int participantCount;

    private boolean rulesExplanationNeeded;

    // 🔗 RELATIONS
    @ManyToOne
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

    public Set<AppUser> getParticipants() { return participants; }
    public void setParticipants(Set<AppUser> participants) { this.participants = participants; }

    public GameSession() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }

    public boolean isRulesExplanationNeeded() {
        return rulesExplanationNeeded;
    }

    public void setRulesExplanationNeeded(boolean rulesExplanationNeeded) {
        this.rulesExplanationNeeded = rulesExplanationNeeded;
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

    public AppUser getCreator() {
        return creator;
    }

    public void setCreator(AppUser creator) {
        this.creator = creator;
    }

    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Transient
    public Set<Long> getParticipantIds() {
        return participants.stream()
                        .map(AppUser::getId)
                        .collect(Collectors.toSet());
    }


    @Override
    public String toString() {
    return "GameSession [id=" + id + ", startTime=" + startTime + ", minPlayers=" + minPlayers + ", maxPlayers="
                + maxPlayers + ", estimatedDuration=" + estimatedDuration + ", description=" + description
                + ", participantCount=" + participantCount + ", rulesExplanationNeeded=" + rulesExplanationNeeded
                + ", event=" + event + ", boardgame=" + boardgame + ", creator=" + creator + "]";
    }


}