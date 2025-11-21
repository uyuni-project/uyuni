/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.kickstart;

import com.redhat.rhn.domain.token.Token;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;


public class KickstartDefaultRegTokenId implements Serializable {

    @Serial
    private static final long serialVersionUID = -3561835819701842114L;

    private KickstartData ksdata;

    private Token token;

    /**
     * Constructor
     */
    public KickstartDefaultRegTokenId() {
    }

    /**
     * Constructor
     *
     * @param ksdataIn the input ksdata
     * @param tokenIn  the input token
     */
    public KickstartDefaultRegTokenId(KickstartData ksdataIn, Token tokenIn) {
        ksdata = ksdataIn;
        token = tokenIn;
    }

    public KickstartData getKsdata() {
        return ksdata;
    }

    public void setKsdata(KickstartData ksdataIn) {
        ksdata = ksdataIn;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token tokenIn) {
        token = tokenIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof KickstartDefaultRegTokenId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(ksdata, that.ksdata)
                .append(token, that.token)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(ksdata)
                .append(token)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "KickstartDefaultRegTokenId{" +
                "ksdata=" + ksdata +
                ", token=" + token +
                '}';
    }
}
