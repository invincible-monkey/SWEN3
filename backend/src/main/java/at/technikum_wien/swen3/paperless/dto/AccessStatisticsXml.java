package at.technikum_wien.swen3.paperless.dto;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "accessStatistics")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccessStatisticsXml {

    @XmlAttribute(name = "date")
    private String date;

    @XmlAttribute(name = "source")
    private String source;

    @XmlElementWrapper(name = "entries")
    @XmlElement(name = "entry")
    private List<AccessEntryXml> entries;
}
