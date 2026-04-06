package backend.playsembly.domain.bgg;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Year;

@Entity
@Table(indexes = {
    @Index(name = "idx_boardgame_name", columnList = "name")
})
public class BoardGame {

    @Id
    private Long bggId;

    @Column(nullable = false)
    @NotBlank(message = "Board game name is required")
    @Size(max = 255, message = "Board game name can be at most 255 characters")
    private String name;

    @Column
    @Min(value = 0, message = "Year published cannot be negative")
    private int yearPublished;

    @Column(length = 1000)
    @Size(max = 1000, message = "Image URL can be at most 1000 characters")
    private String imageUrl;

    @Column(length = 10000)
    @Size(max = 10000, message = "Description can be at most 10000 characters")
    private String description;

    @Column
    @Min(value = 1, message = "Minimum players must be at least 1")
    private int minPlayers;

    @Column
    @Min(value = 1, message = "Maximum players must be at least 1")
    private int maxPlayers;

    public BoardGame() {
    }

    public Long getBggId() {
        return bggId;
    }

    public void setBggId(Long bggId) {
        this.bggId = bggId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getYearPublished() {
        return yearPublished;
    }

    public void setYearPublished(int yearPublished) {
        this.yearPublished = yearPublished;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @PrePersist
    @PreUpdate
    private void validateLogic() {
        int currentYear = Year.now().getValue();
        if (yearPublished > currentYear) {
            throw new IllegalArgumentException("Year published cannot be in the future");
        }
        if (maxPlayers < minPlayers) {
            throw new IllegalArgumentException("Max players cannot be less than min players");
        }
    }

    @Override
    public String toString() {
        return "BoardGame [bggId=" + bggId + ", name=" + name + ", yearPublished=" + yearPublished +
               ", minPlayers=" + minPlayers + ", maxPlayers=" + maxPlayers + "]";
    }
}