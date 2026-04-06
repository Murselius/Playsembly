package backend.playsembly.domain.bgg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) //vastine sisältää paljon sälää, ohitetaan ne mitä ei haluta
public class SearchResponse {

    //Lisätään total-attribuutti, tulosten lukumäärä
    @JacksonXmlProperty(isAttribute = true)
    private int total;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    private List<SearchItem> item;

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public List<SearchItem> getItem() { return item; }
    public void setItem(List<SearchItem> item) { this.item = item; }
}