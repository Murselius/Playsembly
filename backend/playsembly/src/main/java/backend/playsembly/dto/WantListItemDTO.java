package backend.playsembly.dto;

public class WantListItemDTO {
    private Long bggId;
    private String name;
    private String imageUrl;
    private String username;

    public WantListItemDTO() {}

    public WantListItemDTO(Long bggId, String name, String imageUrl, String username) {
        this.bggId = bggId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.username = username;
    }

    public Long getBggId() { return bggId; }
    public void setBggId(Long bggId) { this.bggId = bggId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}