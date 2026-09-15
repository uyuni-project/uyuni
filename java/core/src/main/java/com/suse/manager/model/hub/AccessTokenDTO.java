/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.model.hub;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

public class AccessTokenDTO {

    private long id;

    private String serverFqdn;

    private TokenType type;

    private String localizedType;

    private boolean valid;

    private Date expirationDate;

    private Date creationDate;

    private Date modificationDate;

    private Long hubId;

    private Long peripheralId;

    /**
     * Default constructor
     * @param idIn the token id
     * @param serverFqdnIn the fqdn of the server associated with the token
     * @param typeIn the token type
     * @param validIn true if it is valid
     * @param expirationDateIn the expiration date
     * @param creationDateIn the creation date
     * @param modificationDateIn the last modification date
     * @param hubIdIn the id of the hub, if the fqdn is registered as a hub. null otherwise
     * @param peripheralIdIn the id of the peripheral, if the fqdn is registered as a peripheral. null otherwise
     */
    public AccessTokenDTO(long idIn, String serverFqdnIn, TokenType typeIn, boolean validIn, Date expirationDateIn,
                          Date creationDateIn, Date modificationDateIn, Long hubIdIn, Long peripheralIdIn) {
        this.id = idIn;
        this.serverFqdn = serverFqdnIn;
        this.type = typeIn;
        this.localizedType = typeIn.getDescription();
        this.valid = validIn;
        // Create new instance to ensure this are java.util.Date otherwise GSON will not apply the type adapter
        this.expirationDate = new Date(expirationDateIn.getTime());
        this.creationDate = new Date(creationDateIn.getTime());
        this.modificationDate = new Date(modificationDateIn.getTime());
        this.hubId = hubIdIn;
        this.peripheralId = peripheralIdIn;
    }


    public long getId() {
        return id;
    }

    public void setId(long idIn) {
        this.id = idIn;
    }

    public String getServerFqdn() {
        return serverFqdn;
    }

    public void setServerFqdn(String serverFqdnIn) {
        this.serverFqdn = serverFqdnIn;
    }

    public TokenType getType() {
        return type;
    }


    /**
     * Set the type and update the localized label
     * @param typeIn the token type
     */
    public void setType(TokenType typeIn) {
        this.type = typeIn;
        this.localizedType = type != null ? type.getDescription() : null;
    }

    public String getLocalizedType() {
        return localizedType;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean validIn) {
        this.valid = validIn;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDateIn) {
        this.expirationDate = expirationDateIn;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDateIn) {
        this.creationDate = creationDateIn;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDateIn) {
        this.modificationDate = modificationDateIn;
    }

    public Long getHubId() {
        return hubId;
    }

    public void setHubId(Long hubIdIn) {
        this.hubId = hubIdIn;
    }

    public Long getPeripheralId() {
        return peripheralId;
    }

    public void setPeripheralId(Long peripheralIdIn) {
        this.peripheralId = peripheralIdIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AccessTokenDTO that)) {
            return false;
        }

        return new EqualsBuilder()
            .append(getId(), that.getId())
            .append(isValid(), that.isValid())
            .append(getServerFqdn(), that.getServerFqdn())
            .append(getType(), that.getType())
            .append(getExpirationDate(), that.getExpirationDate())
            .append(getCreationDate(), that.getCreationDate())
            .append(getModificationDate(), that.getModificationDate())
            .append(getHubId(), that.getHubId())
            .append(getPeripheralId(), that.getPeripheralId())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getId())
            .append(getServerFqdn())
            .append(getType())
            .append(isValid())
            .append(getExpirationDate())
            .append(getCreationDate())
            .append(getModificationDate())
            .append(getHubId())
            .append(getPeripheralId())
            .toHashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AccessTokenDTO{");
        sb.append("id=").append(id);
        sb.append(", serverFqdn='").append(serverFqdn).append('\'');
        sb.append(", type=").append(type);
        sb.append(", valid=").append(valid);
        sb.append(", expirationDate=").append(expirationDate);
        sb.append(", creationDate=").append(creationDate);
        sb.append(", modificationDate=").append(modificationDate);
        sb.append(", hubId=").append(hubId);
        sb.append(", peripheralId=").append(peripheralId);
        sb.append('}');
        return sb.toString();
    }
}
