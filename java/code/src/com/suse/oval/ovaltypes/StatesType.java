package com.suse.oval.ovaltypes;

import com.suse.oval.ovaltypes.linux.DpkginfoState;
import com.suse.oval.ovaltypes.linux.RpminfoState;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * The StatesType is a container for one or more state child elements.
 * Each state provides details about specific characteristics that can be used during an evaluation of an object.
 * <p>
 * Please refer to the description of the state element for more information about an individual state.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatesType", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class StatesType {

    @XmlElements({
            @XmlElement(name = "rpminfo_state", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5#linux", type = RpminfoState.class),
            @XmlElement(name = "dpkginfo_state", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5#linux", type = DpkginfoState.class),
            @XmlElement(name = "state", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5", type = StateType.class)
    })
    protected List<StateType> states;

    /**
     * Gets the contained states.
     */
    public List<StateType> getStates() {
        if (states == null) {
            states = new ArrayList<>();
        }
        return this.states;
    }

}
