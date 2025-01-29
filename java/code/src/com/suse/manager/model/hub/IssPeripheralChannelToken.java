/*
 * Copyright (c) 2024--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.model.hub;

import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenParser;
import com.suse.manager.webui.utils.token.TokenParsingException;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name = "suseISSPeripheralChannelToken")
public class IssPeripheralChannelToken {

    private long id;

    private String token;

    private Date expirationDate;

    private IssPeripheralChannels peripheralChannel;

    private boolean valid;

    /**
     * Default constructor
     */
    protected IssPeripheralChannelToken() {
        // Used by Hibernate
    }

    /**
     * Build a new access token
     * @param tokenIn the token
     * @param expirationDateIn the instant the token expires
     */
    public IssPeripheralChannelToken(String tokenIn, Date expirationDateIn) {
        this.token = tokenIn;
        this.expirationDate = expirationDateIn;
        this.valid = true;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long idIn) {
        this.id = idIn;
    }

    @Column(name = "token")
    public String getToken() {
        return token;
    }

    @OneToOne(targetEntity = IssPeripheralChannels.class)
    public IssPeripheralChannels getPeripheralChannel() {
        return peripheralChannel;
    }

    public void setPeripheralChannel(IssPeripheralChannels peripheralChannelIn) {
        this.peripheralChannel = peripheralChannelIn;
    }

    public void setToken(String tokenIn) {
        this.token = tokenIn;
    }

    @Column(name = "expiration_date")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDateIn) {
        this.expirationDate = expirationDateIn;
    }

    @Column(name = "valid")
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean validIn) {
        this.valid = validIn;
    }

    /**
     * Checks if the current instance is expired.
     * @return true if the current date is after the expiration date
     */
    @Transient
    public boolean isExpired() {
        if (expirationDate == null) {
            return false;
        }

        return new Date().after(expirationDate);
    }

    /**
     * Retrieve the parsed token associated with this entity
     * @return the parsed token
     * @throws TokenParsingException if parsing the serialized value fails
     */
    @Transient
    public Token getParsedToken() throws TokenParsingException {
        return new TokenParser()
            .usingServerSecret()
            .verifyingNotBefore()
            .verifyingExpiration()
            .parse(token);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IssPeripheralChannelToken that)) {
            return false;
        }
        return Objects.equals(getToken(), that.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getToken());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IssPeripheralChannelToken{");
        sb.append('}');
        return sb.toString();
    }
}
