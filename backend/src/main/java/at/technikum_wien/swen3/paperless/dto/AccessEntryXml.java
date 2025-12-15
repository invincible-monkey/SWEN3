package at.technikum_wien.swen3.paperless.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class AccessEntryXml {

    @XmlElement(name = "documentId")
    private Long documentId;

    @XmlElement(name = "accessCount")
    private Long accessCount;
}
