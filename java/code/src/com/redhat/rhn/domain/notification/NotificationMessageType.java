/**
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
package com.redhat.rhn.domain.notification;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.config.ConfigChannelType;
import com.redhat.rhn.domain.config.ConfigurationFactory;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * NotificationMessageType - Class representation of the table suseNotificationMessageType.
 * @version $Rev$
 */
public class NotificationMessageType extends BaseDomainHelper {

    private Long id;
    private String label;
    private String name;
    private Long priority;

    public static final String INFO = "info";
    public static final String WARNING = "warning";
    public static final String ERROR = "error";

    private static final Map POSSIBLE_TYPES = new TreeMap(String.CASE_INSENSITIVE_ORDER);

    /**
    *
    * @return the info message type object
    */
   public static NotificationMessageType info() {
       return lookup(INFO);
   }

   /**
    *
    * @return the warning message type object
   */
   public static NotificationMessageType warning() {
       return lookup(WARNING);
   }

   /**
    *
    * @return the error message type object
   */
   public static NotificationMessageType error() {
       return lookup(ERROR);
   }

   /**
    * Given a label type label it returns the associated
    * NotificationMessageType
    * @param type the message type label
    * @return the notification message type associated to the type label.
    */
    public static NotificationMessageType lookup(String type) {
        if (POSSIBLE_TYPES.isEmpty()) {
            NotificationMessageType info = NotificationMessageFactory.
                            lookupNotificationMessageTypeByLabel(INFO);
            NotificationMessageType warning = NotificationMessageFactory.
                            lookupNotificationMessageTypeByLabel(WARNING);
            NotificationMessageType error = NotificationMessageFactory.
                            lookupNotificationMessageTypeByLabel(ERROR);
            POSSIBLE_TYPES.put(INFO, info);
            POSSIBLE_TYPES.put(warning, warning);
            POSSIBLE_TYPES.put(ERROR, error);
       }

       if (!POSSIBLE_TYPES.containsKey(type)) {
           String msg = "Invalid type [" + type + "] specified. " +
           "Make sure you specify one of the following types " +
               "in your expression " + POSSIBLE_TYPES.keySet();
           throw new IllegalArgumentException(msg);
       }
       return (NotificationMessageType) POSSIBLE_TYPES.get(type);
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
     * Getter for label
     * @return String to get
    */
    public String getLabel() {
        return this.label;
    }

    /**
     * Setter for label
     * @param labelIn to set
    */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     * Getter for name
     * @return String to get
    */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for name
     * @param nameIn to set
    */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * Getter for priority
     * @return Long to get
    */
    public Long getPriority() {
        return this.priority;
    }

    /**
     * Setter for priority
     * @param priorityIn to set
    */
    public void setPriority(Long priorityIn) {
        this.priority = priorityIn;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object arg) {
        NotificationMessageType that = (NotificationMessageType) arg;
        return new EqualsBuilder().
                append(this.getLabel(), that.getLabel()).
                append(this.getPriority(), that.getPriority()).
                isEquals();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().
                    append(this.getLabel()).
                    append(this.getPriority()).
                    toHashCode();
    }
}
