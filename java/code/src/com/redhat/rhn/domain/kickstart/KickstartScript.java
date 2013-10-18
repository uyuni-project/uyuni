/**
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.domain.kickstart;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.frontend.dto.BaseDto;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * KickstartScript - Class representation of the table rhnKickstartScript.
 * @version $Rev: 1 $
 */
public class KickstartScript extends BaseDto implements Comparable<KickstartScript> {

    public static final String TYPE_PRE = "pre";
    public static final String TYPE_POST = "post";
    private static final String BASH = "bash";
    private static final String PRE = "Pre";
    private static final String POST = "Post";
    private static final String NOCHROOTPOST = "Nochroot Post";

    private Long id;
    private Long position;
    private String scriptType;
    private String chroot;
    private Boolean errorOnFail = false;
    private String interpreter;
    private String scriptName;
    private byte[] data;
    private Date created;
    private Date modified;
    private Boolean raw = true;
    private boolean editable = true;

    private KickstartData ksdata;

    /**
     * @return True if the script is editable
     */
    public boolean getEditable() {
        return this.editable;
    }

    /**
     * @param editableIn Set if script is editable
     */
    public void setEditable(boolean editableIn) {
        this.editable = editableIn;
    }

    /** Setup the default value for
     * chroot and other fields.
     */
    public KickstartScript() {
        this.chroot = "Y";
        this.scriptType = TYPE_PRE;
    }

    /**
     * Getter for scriptName
     * @return String to get
    */
    public String getScriptName() {
        return this.scriptName;
    }

    /**
     * Setter for scriptName
     * @param scriptNameIn to set
    */
    public void setScriptName(String scriptNameIn) {
        this.scriptName = scriptNameIn;
    }

    /**
     * Getter for id
     * @return Long to get
    */
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for id
     * @param idIn to set
    */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Getter for position
     * @return Long to get
    */
    public Long getPosition() {
        return this.position;
    }

    /**
     * Setter for position
     * @param positionIn to set
    */
    public void setPosition(Long positionIn) {
        this.position = positionIn;
    }

    /**
     * Getter for scriptType
     * @return String to get
    */
    public String getScriptType() {
        return this.scriptType;
    }

    /**
     * Has to have the horrible name because "isChroot" is magic
     * and causes a "can't cast boolean to String" error in hibernate.
     * @return true if the script is run in a chrooted environment
     */
    public boolean thisScriptIsChroot() {
        return !this.chroot.toLowerCase().equals("n");
    }

    /**
     * @return script "prettyified" type for this script
     */
    public String getPrettyScriptType() {
        if (this.scriptType.equals(TYPE_POST)) {
            if (!this.thisScriptIsChroot()) {
                return NOCHROOTPOST;
            }
            return POST;
        }
        return PRE;
    }

    /**
     * Setter for scriptType
     * @param scriptTypeIn to set
    */
    public void setScriptType(String scriptTypeIn) {
        if (!(scriptTypeIn.equals(TYPE_PRE) || scriptTypeIn.equals(TYPE_POST))) {
            throw new IllegalArgumentException("Invalid script type");
        }
        this.scriptType = scriptTypeIn;
    }

    /**
     * Getter for chroot
     * @return String to get
    */
    public String getChroot() {
        return this.chroot;
    }

    /**
     * Setter for chroot
     * @param chrootIn to set
    */
    public void setChroot(String chrootIn) {
        this.chroot = chrootIn;
    }

    /**
     * Getter for errorOnFail
     * @return Boolean to get
     */
    public Boolean getErrorOnFail() {
        return this.errorOnFail;
    }

    /**
     * Setter for errorOnFail
     * @param errorOnFailIn to set
     */
    public void setErrorOnFail(Boolean errorOnFailIn) {
        this.errorOnFail = errorOnFailIn;
    }

    /**
     * Getter for interpreter
     * @return String to get
    */
    public String getInterpreter() {
        return this.interpreter;
    }

    /**
     *
     * @return interpreter for this script
     */
    public String getPrettyInterpreter() {
        if (StringUtils.isBlank(this.interpreter)) {
            return BASH;
        }
        return interpreter;
    }

    /**
     * Setter for interpreter
     * @param interpreterIn to set
    */
    public void setInterpreter(String interpreterIn) {
        this.interpreter = interpreterIn;
    }

    /**
     * Get the String version of the pre contents
     * @return String version of the pre contents
     */
    public String getDataContents() {
        return HibernateFactory.getByteArrayContents(this.getData());
    }

    /**
     * Getter for created
     * @return Date to get
    */
    public Date getCreated() {
        return this.created;
    }

    /**
     * Setter for created
     * @param createdIn to set
    */
    public void setCreated(Date createdIn) {
        this.created = createdIn;
    }

    /**
     * Getter for modified
     * @return Date to get
    */
    public Date getModified() {
        return this.modified;
    }

    /**
     * Setter for modified
     * @param modifiedIn to set
    */
    public void setModified(Date modifiedIn) {
        this.modified = modifiedIn;
    }


    /**
     * @return the ksdata
     */
    public KickstartData getKsdata() {
        return ksdata;
    }


    /**
     * @param ksdataIn The ksdata to set.
     */
    public void setKsdata(KickstartData ksdataIn) {
        this.ksdata = ksdataIn;
    }


    /**
     * @return the data
     */
    public byte[] getData() {
        if (data == null) {
            return new byte[0];
        }
        return data;
    }


    /**
     * @param dataIn The data to set.
     */
    public void setData(byte[] dataIn) {
        this.data = dataIn;
    }

    /**
     * Clone/copy this KickstartScript into a new instance.
     *
     * @param ksDataIn that will own this new KickstartScript
     * @return KickstartScript object that is a copy
     */
    public KickstartScript deepCopy(KickstartData ksDataIn) {
        KickstartScript cloned = new KickstartScript();
        cloned.setChroot(this.getChroot());
        cloned.setScriptName(this.getScriptName());
        cloned.setData(this.getData());
        cloned.setInterpreter(this.getInterpreter());
        cloned.setKsdata(ksDataIn);
        cloned.setPosition(this.getPosition());
        cloned.setScriptType(this.getScriptType());
        cloned.setRaw(this.getRaw());
        cloned.setErrorOnFail(this.getErrorOnFail());
        return cloned;
    }

    /**
     *
     * @param scriptIn KickstartScript to compare order to
     * @return the position order of this script
     */
    public int compareTo(KickstartScript scriptIn) {
        final int before = -1;
        final int after = 1;

        // pre scripts always come first
        if (!scriptIn.getScriptType().equals(this.getScriptType())) {
            if (scriptIn.getScriptType().equals(TYPE_PRE)) {
                return after;
            }
            return before;
        }

        // If both positions are negative then -2 should come *after* -1.
        // This is so that we can give a new script the most negative number
        // and have it come right before Red Hat's scripts, preserving
        // existing behavior.
        if (scriptIn.getPosition() < 0 && this.getPosition() < 0) {
            if (scriptIn.getPosition() > this.getPosition()) {
                return after;
            }
            return before;
        }

        if (scriptIn.getPosition() < this.getPosition()) {
            return after;
        }
        return before;
    }


    /**
     * @return Returns the raw.
     */
    public Boolean getRaw() {
        return raw;
    }


    /**
     * @param rawIn The raw to set.
     */
    public void setRaw(Boolean rawIn) {
        this.raw = rawIn;
    }

}
