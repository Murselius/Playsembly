package backend.playsembly.domain.bgg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class Thumbnail {

    @JacksonXmlText
    private String value;

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}