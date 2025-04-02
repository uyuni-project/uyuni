/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.config;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Hibernate CodeGenerator
 */
@Entity
@Table(name = "RHNCONFIGFILETYPE")
public class ConfigFileType extends BaseDomainHelper {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3816155923541633076L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Equivalent to <generator class="native"/>
    @Column(name = "ID")
    private Long id;

    @Column(name = "LABEL", length = 64, nullable = false)
    private String label;

    @Column(name = "NAME", length = 256, nullable = false)
    private String name;

    public static final String FILE = "file";
    public static final String DIR = "directory";
    public static final String SYMLINK = "symlink";
    public static final String SLS = "sls";
    private static final Map<String, ConfigFileType> POSSIBLE_TYPES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * @return symlink config file type
     */
    public static ConfigFileType symlink() {
        return lookup(SYMLINK);
    }

    /**
     *
     * @return dir config file type
     */
    public static ConfigFileType dir() {
        return lookup(DIR);
    }

    /**
     *
     * @return file config file type
     */
    public static ConfigFileType file() {
        return lookup(FILE);
    }

    /**
     *
     * @return file config SLS type
     */
    public static ConfigFileType sls() {
        return lookup(SLS);
    }

    /**
     * Given a file type label it returns the associated
     * file type object
     * @param type the file type label
     * @return the file type associated to the label.
     */
    public static ConfigFileType lookup(String type) {
        if (POSSIBLE_TYPES.isEmpty()) {
            ConfigFileType file = ConfigurationFactory.
                                    lookupConfigFileTypeByLabel(FILE);
            ConfigFileType sls = ConfigurationFactory.
                    lookupConfigFileTypeByLabel(SLS);
            ConfigFileType dir = ConfigurationFactory.
                            lookupConfigFileTypeByLabel(DIR);
            ConfigFileType symlink = ConfigurationFactory.
                            lookupConfigFileTypeByLabel(SYMLINK);
            POSSIBLE_TYPES.put(DIR, dir);
            POSSIBLE_TYPES.put("dir", dir);
            POSSIBLE_TYPES.put("folder", dir);

            POSSIBLE_TYPES.put(FILE, file);

            POSSIBLE_TYPES.put(SYMLINK, symlink);

            POSSIBLE_TYPES.put(SLS, sls);
        }

        if (!POSSIBLE_TYPES.containsKey(type)) {
            String msg = "Invalid type [" + type + "] specified. " +
                            "Make sure you specify one of the following types " +
                            "in your expression " + POSSIBLE_TYPES.keySet();
            throw new IllegalArgumentException(msg);
        }
        return POSSIBLE_TYPES.get(type);
    }

    /**
     * Ctor for Hibernate
     * @param inLabel label
     * @param inName  name
     * @param inCreated when created
     * @param inModified last modified
     */
    protected ConfigFileType(java.lang.String inLabel, java.lang.String inName,
            java.util.Date inCreated, java.util.Date inModified) {
        this.label = inLabel;
        this.name = inName;
        this.setCreated(inCreated);
        this.setModified(inModified);
    }

    /**
     * default ctor
     */
    protected ConfigFileType() {
    }

    /**
     * Get DB id
     * @return db id
     */
    public long getId() {
        return this.id;
    }

    /**
     * Set the id column
     * @param inId new DB id
     */
    public void setId(long inId) {
        this.id = inId;
    }

    /**
     * Get the label
     * @return label
     */
    public java.lang.String getLabel() {
        return this.label;
    }

    /**
     * Set the label
     * @param inLabel new label
     */
    public void setLabel(java.lang.String inLabel) {
        this.label = inLabel;
    }

    /**
     * Get the name
     * @return name
     */
    public java.lang.String getName() {
        return this.name;
    }

    /**
     * Set the name
     * @param inName new name
     */
    public void setName(java.lang.String inName) {
        this.name = inName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ConfigFileType castOther)) {
            return false;
        }
        return new EqualsBuilder().append(this.getId(), castOther.getId()).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }

    /**
     * @return The i18n message key for the type
     */
    public String getMessageKey() {
        if (dir().equals(this)) {
            return "addfiles.jsp.type.directory";
        }
        else if (symlink().equals(this)) {
            return "addfiles.jsp.type.symlink";
        }
        return "addfiles.jsp.type.text";
    }

}
