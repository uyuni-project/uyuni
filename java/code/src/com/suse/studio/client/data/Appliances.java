package com.suse.studio.client.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "appliances")
@XmlAccessorType(XmlAccessType.FIELD)
public class Appliances {

    @XmlElement(name = "appliance")
    private List<Appliance> appliances;

    public List<Appliance> getAppliances() {
        if (appliances == null) {
            appliances = new ArrayList<Appliance>();
        }
        return this.appliances;
    }
}
