package com.suse.studio.client.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "builds")
public class Builds {

    @XmlElement(name = "build")
    private List<Build> builds;

    public List<Build> getBuilds() {
        if (this.builds == null) {
            this.builds = new ArrayList<Build>();
        }
        return this.builds;
    }
}
