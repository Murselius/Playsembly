package backend.playsembly.domain.bgg;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(indexes = {
    @Index(name = "idx_boardgame_name", columnList = "name")
})
public class BoardGame {

    @Id
    private Long bggId;

    private String name;

    @Column
    private int yearPublished;


    @Column(length = 1000)
    private String imageUrl;

    @Column(length = 5000)
    private String description;

    private int minPlayers;

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

}