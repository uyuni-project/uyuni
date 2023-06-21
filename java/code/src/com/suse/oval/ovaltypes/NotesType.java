package com.suse.oval.ovaltypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * The NotesType complex type is a container for one or more note child elements. Each note contains some information
 * about the definition or tests that it references. A note may record an unresolved question about the definition or
 * test or present the reason as to why a particular approach was taken.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NotesType", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class NotesType {

    @XmlElement(name = "note", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5", required = true)
    protected List<String> notes;

    /**
     * Gets the list of notes
     */
    public List<String> getNotes() {
        if (notes == null) {
            notes = new ArrayList<>();
        }
        return this.notes;
    }

}
