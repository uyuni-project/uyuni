package com.suse.studio.client.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "build")
public class Build {

    @XmlElement(name = "id")
    private String id;
	
    @XmlElement(name = "version")
    private String version;

    @XmlElement(name = "image_type")
    private String imageType;

    @XmlElement(name = "download_url")
    private String downloadURL;

    public String getId() {
    	return this.id;
    }
    
    public String getVersion() {
        return this.version;
    }

    public String getImageType() {
        return this.imageType;
    }

    public String getDownloadURL() {
        return this.downloadURL;
    }
}
