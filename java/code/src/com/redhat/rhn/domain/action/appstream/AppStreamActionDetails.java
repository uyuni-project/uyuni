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

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Represents details of an AppStream action.
 */
@Entity
@Table(name = "suseActionAppstream")
public class AppStreamActionDetails extends BaseDomainHelper {

    private static final String DISABLE_TYPE = "DISABLE";
    private static final String ENABLE_TYPE = "ENABLE";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suse_act_appstream_seq")
    @SequenceGenerator(name = "suse_act_appstream_seq", sequenceName = "suse_act_appstream_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "module_name")
    private String moduleName;

    @Column
    private String stream;

    @Column
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", updatable = false, nullable = false)
    private Action parentAction;

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

    protected void setId(Long idIn) {
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

    /**
     * Gets the parent Action associated with this ServerAction record
     * @return Returns the parentAction.
     */
    public Action getParentAction() {
        return parentAction;
    }

    /**
     * Sets the parent Action associated with this ServerAction record
     * @param parentActionIn The parentAction to set.
     */
    public void setParentAction(Action parentActionIn) {
        this.parentAction = parentActionIn;
    }
}
