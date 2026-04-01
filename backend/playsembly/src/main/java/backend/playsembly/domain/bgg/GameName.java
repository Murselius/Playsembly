package backend.playsembly.domain.bgg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GameName {

    @JacksonXmlProperty(isAttribute = true, localName = "value")
    private String value;

    @JacksonXmlProperty(isAttribute = true)
    private String type; // primary, alternate, etc.

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}