package backend.playsembly.domain.bgg;

public class SearchResultDTO {

    private Long id;
    private String name;
    private String year;
    private String imageUrl;

    public SearchResultDTO(Long id, String name, String year, String imageUrl) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.imageUrl = imageUrl;
    }

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}