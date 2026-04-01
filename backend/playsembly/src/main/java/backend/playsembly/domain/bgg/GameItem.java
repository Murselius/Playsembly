package backend.playsembly.domain.bgg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.*;
import java.util.List;

import org.springframework.cache.Cache.ValueWrapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GameItem {

    @JacksonXmlProperty(isAttribute = true)
    private Long id;

    @JacksonXmlProperty(localName = "image")
    private Image image;

    private String description;

    @JacksonXmlProperty(localName = "thumbnail")
    private Thumbnail thumbnail;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "name")
    private List<GameName> names;

    @JacksonXmlProperty(localName = "yearpublished")
    private ValueField yearpublished;

    @JacksonXmlProperty(localName = "minplayers")
    private ValueField minplayers;

    @JacksonXmlProperty(localName = "maxplayers")
    private ValueField maxplayers;

    public Long getId() {
        return id;
    }

    public List<GameName> getNames() {
        return names;
    }

    public ValueField getYearpublished() {
        return yearpublished;
    }

    public ValueField getMinplayers() {
        return minplayers;
    }

    public ValueField getMaxplayers() {
        return maxplayers;
    }

    public Thumbnail getThumbnail() { return thumbnail; }
    public void setThumbnail(Thumbnail thumbnail) { this.thumbnail = thumbnail; }

    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }

    public String getDescription() {
    return description;
}

    // Helper method
   public String getPrimaryName() {
    return names.stream()
            .filter(n -> "primary".equals(n.getType()))
            .map(GameName::getValue)
            .findFirst()
            .orElse(names.get(0).getValue());
}
}