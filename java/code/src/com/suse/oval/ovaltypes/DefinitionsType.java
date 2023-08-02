package com.suse.oval.ovaltypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * The DefinitionsType complex type is a container for one or more definition elements.
 * Each definition element describes a single OVAL Definition.
 * <p>
 * Please refer to the description of the {@link DefinitionType} for more information about an individual definition.
 * </p>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DefinitionsType", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class DefinitionsType {

    @XmlElement(name = "definition", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5", required = true)
    protected List<DefinitionType> definitions;

    /**
     * Gets the list of definitions
     */
    public List<DefinitionType> getDefinitions() {
        if (definitions == null) {
            definitions = new ArrayList<>();
        }
        return this.definitions;
    }

}
