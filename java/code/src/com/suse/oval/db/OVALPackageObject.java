package com.suse.oval.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "suseOVALPackageObject")
public class OVALPackageObject {

    private String id;
    private String packageName;
    private boolean isRpm;

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "name")
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Column(name = "isRpm")
    public boolean isRpm() {
        return isRpm;
    }

    public void setRpm(boolean rpm) {
        isRpm = rpm;
    }
}
