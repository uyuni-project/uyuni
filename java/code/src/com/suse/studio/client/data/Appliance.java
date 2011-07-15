package com.suse.studio.client.data;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "appliance")
public class Appliance {

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "arch")
    private String arch;

    @XmlElement(name = "type")
    private String type;

    @XmlElement(name = "builds")
    private Builds builds;

    public String getName() {
        return this.name;
    }

    public String getArch() {
        return this.arch;
    }

    public String getType() {
        return this.type;
    }

    /**
     * Directly return the contained list.
     * 
     * @return list of builds
     */
    public List<Build> getBuilds() {
        return this.builds.getBuilds();
    }
}
