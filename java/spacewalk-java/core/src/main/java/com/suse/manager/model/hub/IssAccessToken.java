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

import com.redhat.rhn.domain.BaseDomainHelper;

import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenParser;
import com.suse.manager.webui.utils.token.TokenParsingException;

import org.hibernate.annotations.Type;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "suseISSAccessToken")
public class IssAccessToken extends BaseDomainHelper {

    private Long id;

    private String token;

    private TokenType type;

    private String serverFqdn;

    private Date expirationDate;

    private boolean valid;

    /**
     * Default constructor
     */
    protected IssAccessToken() {
        // Used by Hibernate
    }

    /**
     * Build a new access token with the default expiration period of 1 year
     * @param typeIn the type of token
     * @param tokenIn the token
     * @param serverFqdnIn the FQDN of the server related to this token
     */
    public IssAccessToken(TokenType typeIn, String tokenIn, String serverFqdnIn) {
        this(typeIn, tokenIn, serverFqdnIn, Date.from(ZonedDateTime.now().plusYears(1).toInstant()));
    }

    /**
     * Build a new access token
     * @param typeIn the type of token
     * @param tokenIn the token
     * @param serverFqdnIn the FQDN of the server related to this token
     * @param expirationDateIn the instant the token expires
     */
    public IssAccessToken(TokenType typeIn, String tokenIn, String serverFqdnIn, Instant expirationDateIn) {
        this(typeIn, tokenIn, serverFqdnIn, Date.from(expirationDateIn));
    }

    /**
     * Build a new access token
     * @param typeIn the type of token
     * @param tokenIn the token
     * @param serverFqdnIn the FQDN of the server related to this token
     * @param expirationDateIn the instant the token expires
     */
    public IssAccessToken(TokenType typeIn, String tokenIn, String serverFqdnIn, Date expirationDateIn) {
        this.token = tokenIn;
        this.type = typeIn;
        this.serverFqdn = serverFqdnIn;
        this.expirationDate = expirationDateIn;
        this.valid = true;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        this.id = idIn;
    }

    @Column(name = "token")
    public String getToken() {
        return token;
    }

    public void setToken(String tokenIn) {
        this.token = tokenIn;
    }

    @Column(name = "type")
    @Type(value = com.suse.manager.model.hub.TokenTypeEnumType.class)
    public TokenType getType() {
        return type;
    }

    public void setType(TokenType typeIn) {
        this.type = typeIn;
    }

    @Column(name = "server_fqdn")
    public String getServerFqdn() {
        return serverFqdn;
    }

    public void setServerFqdn(String serverFqdnIn) {
        this.serverFqdn = serverFqdnIn;
    }

    @Column(name = "expiration_date")
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
        if (!(o instanceof IssAccessToken issAccessToken)) {
            return false;
        }
        return Objects.equals(getToken(), issAccessToken.getToken()) &&
            Objects.equals(getType(), issAccessToken.getType()) &&
            Objects.equals(getServerFqdn(), issAccessToken.getServerFqdn());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getToken(), getType(), getServerFqdn());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IssAccessToken{id =").append(id);
        sb.append(", type=").append(type);
        sb.append(", serverFqdn='").append(serverFqdn).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
