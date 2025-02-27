/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.server;

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * CustomDataValue
 */
@Entity
@Table(name = "rhnServerCustomDataValue")
public class CustomDataValue extends BaseDomainHelper {
    @Embeddable
    public class ServerCustomDataValueKey implements Serializable {

        @Column(name = "server_id")
        private Server server = new Server();

        @Column(name = "key_id")
        private CustomDataKey key = new CustomDataKey();

        public Server getServer() {
            return server;
        }

        public void setServer(Server serverIn) {
            this.server = serverIn;
        }

        public CustomDataKey getKey() {
            return key;
        }

        public void setKey(CustomDataKey keyIdIn) {
            this.key = keyIdIn;
        }

        @Override
        public boolean equals(Object oIn) {
            if (!(oIn instanceof ServerCustomDataValueKey that)) {
                return false;
            }
            return Objects.equals(server, that.server) && Objects.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(server, key);
        }

    }
    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private ServerCustomDataValueKey id = new ServerCustomDataValueKey();

    @Column(name = "value")
    private String value = "";

    @ManyToOne(targetEntity = UserImpl.class)
    @JoinColumn(name = "created_by")
    private User creator;

    @ManyToOne(targetEntity = UserImpl.class)
    @JoinColumn(name = "last_modified_by")
    private User lastModifier;

    /**
     * @return Returns the id.
     */
    public ServerCustomDataValueKey getId() {
        return id;
    }

    /**
     * @param idIn The id to set.
     */
    public void setId(ServerCustomDataValueKey idIn) {
        id = idIn;
    }

    /**
     * @return Returns the creator.
     */
    public User getCreator() {
        return creator;
    }
    /**
     * @param creatorIn The creator to set.
     */
    public void setCreator(User creatorIn) {
        this.creator = creatorIn;
        //the creator is also the first and last modifier by now
        this.setLastModifier(this.creator);
    }
    /**
     * @return Returns the key.
     */
    public CustomDataKey getKey() {
        return id.getKey();
    }
    /**
     * @param keyIn The key to set.
     */
    public void setKey(CustomDataKey keyIn) {
        this.id.setKey(keyIn);
    }
    /**
     * @return Returns the lastModifier.
     */
    public User getLastModifier() {
        return lastModifier;
    }
    /**
     * @param lastModifierIn The lastModifier to set.
     */
    public void setLastModifier(User lastModifierIn) {
        this.lastModifier = lastModifierIn;
    }
    /**
     * @return Returns the server.
     */
    public Server getServer() {
        return id.getServer();
    }
    /**
     * @param serverIn The server to set.
     */
    public void setServer(Server serverIn) {
        this.id.setServer(serverIn);
    }
    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }
    /**
     * @param valueIn The value to set.
     */
    public void setValue(String valueIn) {
        this.value = StringUtil.webToLinux(valueIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof CustomDataValue castOther)) {
            return false;
        }
        return new EqualsBuilder().append(this.getKey(), castOther.getKey())
                .append(this.getServer(), castOther.getServer()).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getKey().getId()).append(this.getServer().getId())
                .toHashCode();
    }
}
