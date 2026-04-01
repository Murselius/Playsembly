package backend.playsembly.domain.bgg;

import com.fasterxml.jackson.dataformat.xml.annotation.*;

public class ValueField {

    @JacksonXmlProperty(isAttribute = true)
    private String value;

    public String getValue() {
        return value;
    }
}