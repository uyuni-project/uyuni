/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.action.appstream;

import com.redhat.rhn.domain.action.ActionChild;

/**
 * Represents details of an AppStream action.
 */
public class AppStreamActionDetails extends ActionChild {

    private static final String DISABLE_TYPE = "DISABLE";
    private static final String ENABLE_TYPE = "ENABLE";
    private Long id;
    private String moduleName;
    private String stream;
    private String type;

    /**
     * Constructs a new AppStreamActionDetails instance.
     */
    public AppStreamActionDetails() {
        // Default constructor
    }

    /**
     * Constructs a new AppStreamActionDetails instance with specified moduleName, stream, and type.
     *
     * @param moduleNameIn  the appstream module name
     * @param streamIn      the stream
     * @param typeIn        the type of action
     */
    private AppStreamActionDetails(String moduleNameIn, String streamIn, String typeIn) {
        moduleName = moduleNameIn;
        stream = streamIn;
        type = typeIn;
    }

    /**
     * Creates an AppStreamActionDetails instance for disabling an appstream module.
     *
     * @param appStream the string representing the appstream in the format module:stream
     * @return the AppStreamActionDetails instance representing the disable action
     */
    public static AppStreamActionDetails disableAction(String appStream) {
        return new AppStreamActionDetails(appStream.split(":")[0], null, DISABLE_TYPE);
    }

    /**
     * Creates an AppStreamActionDetails instance for enabling an appstream module.
     *
     * @param appStream the string representing the appstream in the format module:stream
     * @return the AppStreamActionDetails instance representing the enable action
     */
    public static AppStreamActionDetails enableAction(String appStream) {
        String[] appStreamData = appStream.split(":");
        return new AppStreamActionDetails(appStreamData[0], appStreamData[1], ENABLE_TYPE);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    public void setModuleName(String moduleNameIn) {
        moduleName = moduleNameIn;
    }

    public void setStream(String streamIn) {
        stream = streamIn;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getStream() {
        return stream;
    }

    public String getType() {
        return type;
    }

    public void setType(String typeIn) {
        type = typeIn;
    }

    public boolean isEnable() {
        return ENABLE_TYPE.equals(type);
    }
}
