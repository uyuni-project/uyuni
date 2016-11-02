/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt;

import com.google.gson.annotations.SerializedName;

/**
 * result of package install/update
 */
public class OldNew {

    @SerializedName("old")
    private final String oldVersion;
    @SerializedName("new")
    private final String newVersion;

    /**
     * constructor
     * @param oldVersionIn old version
     * @param newVersionIn new version
     */
    public OldNew(String oldVersionIn, String newVersionIn) {
        this.oldVersion = oldVersionIn;
        this.newVersion = newVersionIn;
    }

    /**
     * the old version
     * @return old version
     */
    public String getOldVersion() {
        return oldVersion;
    }

    /**
     * the new version
     * @return new version
     */
    public String getNewVersion() {
        return newVersion;
    }
}
