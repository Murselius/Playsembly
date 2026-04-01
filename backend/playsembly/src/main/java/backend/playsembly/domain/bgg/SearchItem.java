package backend.playsembly.domain.bgg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // ohitetaan tuntemattomat kentät
public class SearchItem {

    @JacksonXmlProperty(isAttribute = true)
    private Long id;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "name")
    private List<GameName> names; // 🔹 käytetään omaa GameName-luokkaa

    @JacksonXmlProperty(localName = "yearpublished")
    private ValueField yearpublished;

    @JacksonXmlProperty(localName = "thumbnail")
    private Thumbnail thumbnail;

    @JacksonXmlProperty(localName = "image")
    private Image image;

    // getterit / setterit
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public List<GameName> getNames() { return names; }
    public void setNames(List<GameName> names) { this.names = names; }

    public ValueField getYearpublished() { return yearpublished; }
    public void setYearpublished(ValueField yearpublished) { this.yearpublished = yearpublished; }

    public Thumbnail getThumbnail() { return thumbnail; }
    public void setThumbnail(Thumbnail thumbnail) { this.thumbnail = thumbnail; }

    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }

    // Helper
    public String getPrimaryName() {
        return names.stream()
                .filter(n -> "primary".equals(n.getType()))
                .map(GameName::getValue)
                .findFirst()
                .orElse(names.get(0).getValue());
    }
}