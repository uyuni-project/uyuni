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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;

/**
 * NotificationMessageType - Class representation of the table suseNotificationMessageType.
 * @version $Rev$
 */
@Entity
@Table(name = "susenotificationmessagetype")
public class NotificationMessageType {

    private Long id;
    private String label;
    private String name;
    private Long priority;

    /**
    *
    * @return the info message type object
    */
   public static NotificationMessageType info() {
       return NotificationMessageFactory.lookupNotificationMessageTypeByLabel("info").get();
   }

   /**
    *
    * @return the warning message type object
   */
   public static NotificationMessageType warning() {
       return NotificationMessageFactory.lookupNotificationMessageTypeByLabel("warning").get();
   }

   /**
    *
    * @return the error message type object
   */
   public static NotificationMessageType error() {
       return NotificationMessageFactory.lookupNotificationMessageTypeByLabel("error").get();
   }

    /**
     * Getter for id
     * @return Long to get
    */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notifymsgtype_seq")
    @SequenceGenerator(name = "notifymsgtype_seq",
            sequenceName = "suse_notifymsg_type_id_seq", allocationSize = 1)
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
    @Column(name = "label")
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
    @Column(name = "name")
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
    @Column(name = "priority")
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
