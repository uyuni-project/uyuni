package com.redhat.rhn.manager.audit.scap.xml;

import org.simpleframework.xml.Attribute;

/**
 * Created by matei on 2/14/17.
 */
public class Profile {

    @Attribute
    private String title;

    @Attribute
    private String id;

    @Attribute
    private String description;

    public Profile() {
    }

    public Profile(String id, String title, String description) {
        this.title = title;
        this.id = id;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
